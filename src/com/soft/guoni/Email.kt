package com.soft.guoni

import com.sun.mail.util.MailSSLSocketFactory
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.sql.Connection
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


class Email(val connection: Connection) {
    companion object {
        val smtpHost = "smtp.163.com"
        val pop3Host = "pop.163.com"
        //val usernameSmtp = "13277481910@qq.com"
        val usernameSmtp = "yangglemu131@163.com"
        val passwordSmtp = "yuanbo132"
        val usernamePop = "yangglemu@163.com"
        //val passwordSmtp = "gssqrygvdkdddaaf"
        val passwordPop3 = "yuanbo132"
        val mailBoxPop3 = "yangglemu@163.com"
        val mailBoxSmtp = "yangglemu131@163.com"
        //val mailBoxSmtp = "13277481910@qq.com"
        val subject = "sunshine"
        val formatString = "yyyy-MM-dd"
    }

    fun send(content: String, date: Date) {
        log.info("start to send...")
        log.info("the message size is: ${content.length}")
        val ps = Properties()
        //val factory = MailSSLSocketFactory()
        //factory.isTrustAllHosts = true
        ps.put("mail.smtp.host", smtpHost)
        //ps.put("mail.transport.protocol", "smtp")
        ps.put("mail.smtp.auth", "true")
        //ps.put("mail.smtp.ssl.enable", "true")
        //ps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        // ps.put("mail.smtp.ssl.socketFactory", factory)
        //ps.put("mail.smtp.socketFactory.port", "465")
        ps.put("mail.smtp.port", "25")
        //ps.put("mail.smtp.socketFactory.fallback", "false")
        val session = Session.getInstance(ps)
        //session.debug = true
        val msg = MimeMessage(session)
        msg.setFrom(InternetAddress(mailBoxSmtp))
        msg.setRecipients(Message.RecipientType.TO, mailBoxPop3)
        msg.subject = subject
        msg.sentDate = date
        msg.setText(content)
        msg.saveChanges()
        val trans = session.transport
        trans.connect(usernameSmtp, passwordSmtp)
        try {
            trans.sendMessage(msg, msg.allRecipients)
        } catch (e: Exception) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            log.warning(sw.toString())
            throw Exception(sw.toString())
        } finally {
            trans.close()
        }
        log.info("end to send is ok!")
    }

    fun postMsg(date: Date = Date()) {
        send(document2String(date), date)
        //deleteOldMessage()
        val timer = Timer("deleteMessage")
        timer.schedule(object : TimerTask() {
            override fun run() {
                deleteOldMessage(date)
            }
        }, 7000)
    }

    fun deleteOldMessage(date: Date) {
        log.info("start to delete old message...")
        val session = Session.getInstance(Properties())
        val store = session.getStore("pop3")
        store.connect(pop3Host, usernamePop, passwordPop3)
        val folder = store.getFolder("INBOX")
        folder.open(Folder.READ_WRITE)
        val today = date.toString(formatString)
        val array = ArrayList<Message>()
        for (msg in folder.messages) {
            if (msg.subject != "sunshine" || isOldMessage(msg)) {
                msg.setFlag(Flags.Flag.DELETED, true)
            } else if (msg.sentDate.toString(formatString) == today) {
                array.add(msg)
            }
        }
        val size = array.size
        log.info("today's messages count: $size")
        when {
            (size == 0) -> log.info("today has no message!")
            (size == 1) -> log.info("today only has one message, nothing to do! ")
            (size > 1) -> {
                for (index in 0..size - 2) {
                    array[index].setFlag(Flags.Flag.DELETED, true)
                    log.info("today's old message on ${array[index].sentDate.toString("yyyy-MM-dd HH:mm:ss")} is deleted!")
                }
                log.info("today's new message on ${array[size - 1].sentDate.toString("yyyy-MM-dd HH:mm:ss")} is changed!")
            }
        }
        folder.close(true)
        store.close()
        log.info("end to delete old message is ok!")
        log.fine("\r\n")
    }

    fun isOldMessage(msg: Message): Boolean {
        var isOld = false
        val now = Date().time
        val start = msg.sentDate.time
        val diff = 1000L * 60 * 60 * 24 * 7
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