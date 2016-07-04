package com.soft.guoni

import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.Flags
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Date.toString(formatString: String): String {
    return SimpleDateFormat(formatString).format(this)
}

class Email(val connection: Connection) {
    companion object {
        val smtpHost = "smtp.163.com"
        val pop3Host = "pop.163.com"
        val username = "yangglemu"
        val password = "yuanbo132"
        val mailBox = "yangglemu@163.com"
        val subject = "sunshine"
        val formatString = "yyyy-MM-dd"
    }

    fun send(content: String) {
        val session = Session.getInstance(Properties())
        val msg = MimeMessage(session)
        msg.setFrom(InternetAddress(mailBox))
        msg.subject = subject
        msg.sentDate = Date()
        msg.setText(content)
        val trans = session.getTransport("smtp")
        trans.connect(smtpHost, username, password)
        trans.sendMessage(msg, arrayOf(InternetAddress(mailBox)))
        trans.close()
    }

    fun postMsg() {
        send(document2String(Date()))
        deleteOldMessage()
    }

    fun deleteOldMessage() {
        val session = Session.getInstance(Properties())
        val store = session.getStore("pop3")
        store.connect(pop3Host, username, password)
        val folder = store.getFolder("INBOX")
        folder.open(Folder.READ_WRITE)
        val today = Date().toString(formatString)
        val array = ArrayList<Message>()
        for (msg in folder.messages) {
            if (msg.subject != "sunshine" || isOldMessage(msg)) {
                msg.setFlag(Flags.Flag.DELETED, true)
            } else if (msg.sentDate.toString(formatString) == today) {

                array.add(msg)
            }
        }
        log.info("today's messages count: ${array.size}")
        if (array.size > 1) {
            for (index in 0..array.size - 2) {
                array[index].setFlag(Flags.Flag.DELETED, true)
                log.info("today's old message on ${array[index].sentDate.toString("yyyy-MM-dd HH:mm:ss")} is deleted!")
            }
        }
        log.info("today's new message on ${array[array.size - 1].sentDate.toString("yyyy-MM-dd HH:mm:ss")} is changed!")
        folder.close(true)
        store.close()
    }

    fun isOldMessage(msg: Message): Boolean {
        var isOld = false
        val now = Date().time
        val start = msg.sentDate.time
        val diff = 1000L * 60 * 60 * 24 * 30
        if (now - start > diff) isOld = true
        return isOld
    }

    fun string2Document(content: String): Document {
        val reader = StringReader(content)
        val stream = InputSource(reader)
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream)
        return doc
    }

    fun document2String(date: Date): String {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val root = doc.createElement("duobao")
        doc.appendChild(root)

        val statement = connection.createStatement()
        val res_goods = statement.executeQuery("select sj as tm,sum(kc) as sl from goods group by sj")
        val goods = doc.createElement("goods")
        root.appendChild(goods)
        while (res_goods.next()) {
            val tm = res_goods.getInt("tm").toString()
            val sl = res_goods.getInt("sl").toString()
            val element = doc.createElement("goods")
            element.setAttribute("tm", tm)
            element.setAttribute("sl", sl)
            goods.appendChild(element)
        }
        val res_sale_mx = statement.executeQuery("select sale_mx.id as id,sale_db.rq as rq,sale_mx.sj as tm,sale_mx.sl as sl,"
                + "sale_mx.zq as zq,sale_mx.je as je from sale_mx join sale_db "
                + "on(sale_mx.djh=sale_db.djh) where date(sale_db.rq)='${date.toString(formatString)}'")
        val sale_mx = doc.createElement("sale_mx")

        while (res_sale_mx.next()) {
            val id = res_sale_mx.getString("id")
            val rq = res_sale_mx.getString("rq")
            val tm = res_sale_mx.getString("tm")
            val sl = res_sale_mx.getString("sl")
            val zq = res_sale_mx.getString("zq")
            val je = res_sale_mx.getString("je")
            val element = doc.createElement("sale_mx")
            element.setAttribute("id", id)
            element.setAttribute("rq", rq)
            element.setAttribute("tm", tm)
            element.setAttribute("sl", sl)
            element.setAttribute("zq", zq)
            element.setAttribute("je", je)
            sale_mx.appendChild(element)
        }
        val res_sale_db = statement.executeQuery("select rq,sl,je from sale_db where date(rq)='${date.toString(formatString)}'")
        val sale_db = doc.createElement("sale_db")
        root.appendChild(sale_db)
        while (res_sale_db.next()) {
            val rq = res_sale_db.getString("rq")
            val sl = res_sale_db.getString("sl")
            val je = res_sale_db.getString("je")
            val element = doc.createElement("sale_db")
            element.setAttribute("rq", rq)
            element.setAttribute("sl", sl)
            element.setAttribute("je", je)
            sale_db.appendChild(element)
        }
        res_goods.close()
        res_sale_db.close()
        root.appendChild(sale_mx)
        statement.close()

        val stream = ByteArrayOutputStream()
        val tans = TransformerFactory.newInstance().newTransformer()
        tans.transform(DOMSource(doc), StreamResult(stream))
        val content = stream.toString()
        stream.close()
        return content
    }
}