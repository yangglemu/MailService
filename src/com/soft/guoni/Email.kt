package com.soft.guoni

import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.sql.Connection
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
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
        val smtpPort = 465
        val pop3Port = 995
        val username = "yangglemu"
        val password = "yuanbo132"
        val mailBox = "yangglemu@163.com"
        val subject = "sunshine"
        val timeOfHour = 1000 * 60 * 60
        val formatString = "yyyy-MM-dd"
        val log = Log.log
    }

    fun send(content: String) {
        var p = Properties()
        p.put("mail.smtp.ssl.enable", true)
        p.put("mail.smtp.host", smtpHost)
        p.put("mail.smtp.port", smtpPort)
        p.put("mail.smtp.auth", true)
        var session = Session.getInstance(p, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
        })
        var msg = MimeMessage(session)
        msg.setFrom(InternetAddress(mailBox))
        msg.setRecipients(MimeMessage.RecipientType.TO, mailBox)
        msg.subject = subject
        msg.sentDate = Date()
        msg.setText(content)
        try {
            Transport.send(msg)
        } catch(e: Exception) {
        }
    }

    fun postMsg() {
        val p = Properties()
        p.put("mail.pop3.ssl.enable", true)
        p.put("mail.pop3.host", pop3Host)
        p.put("mail.pop3.port", pop3Port)
        val session = Session.getInstance(p)
        val store = session.getStore("pop3")
        store.connect(username, password)
        val folder = store.getFolder("INBOX")
        folder.open(Folder.READ_WRITE)
        val today = Date().toString(formatString)
        for (msg in folder.messages) {
            if (msg.subject != "sunshine") {
                msg.setFlag(Flags.Flag.DELETED, true)
            } else if (msg.sentDate.toString(formatString) == today) {
                msg.setFlag(Flags.Flag.DELETED, true)
            }
        }
        folder.close(true)
        val content = document2String(Date())
        send(content)
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