package com.soft.yuan

import com.sun.mail.pop3.POP3Folder
import java.io.ByteArrayOutputStream
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Created by 123456 on 2016/6/20.
 */
class Mail {
    companion object {
        val smtpHost = "smtp.163.com"
        val pop3Host = "pop.163.com"
        val smtpPort = 465
        val pop3Port = 995
        val username = "yangglemu"
        val password = "yuanbo132"
        val mailBox = "yangglemu@163.com"
        val url = "jdbc:mysql://pc201408020832:3306/duobao?user=root&password=yuanbo960502"
        val timeOfDay = 1000 * 60 * 60 * 24
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
        msg.subject = "sunshine"
        msg.setText(content)
        Transport.send(msg)
    }

    fun receive(): String {
        var p = Properties()
        p.put("mail.pop3.ssl.enable", true)
        p.put("mail.pop3.host", pop3Host)
        p.put("mail.pop3.port", pop3Port)
        var session = Session.getInstance(p)
        var store = session.getStore("pop3")
        store.connect(username, password)
        var folder = store.getFolder("INBOX") as POP3Folder
        folder.open(Folder.READ_ONLY)
        if (folder.messageCount > 0) {
            for (msg in folder.messages) {
                println(folder.getUID(msg))
            }
        }
        folder.close(false)
        return ""
    }

    fun getXmlContent(): String {
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        var root = doc.createElement("duobao")
        doc.appendChild(root)

        var connection = DriverManager.getConnection(url)
        var statement = connection.createStatement()
        var format = SimpleDateFormat("yyyy-MM-dd")
        var now = Date()
        var res_goods = statement.executeQuery("select sj as tm,sum(kc) as sl from goods group by sj")
        var goods = doc.createElement("goods")
        root.appendChild(goods)
        while (res_goods.next()) {
            if (res_goods.getInt("sl") == 0) continue
            var tm = res_goods.getInt("tm").toString()
            var sl = res_goods.getInt("sl").toString()
            var element = doc.createElement("goods")
            element.setAttribute("tm", tm)
            element.setAttribute("sl", sl)
            goods.appendChild(element)
        }

        var res_sale_mx = statement.executeQuery("select sale_db.rq as rq,sale_mx.sj as tm,sale_mx.sl as sl,sale_mx.zq as zq,sale_mx.je as je from sale_mx join sale_db "
                + "on(sale_mx.djh=sale_db.djh) where date(sale_db.rq)='${format.format(now)}'")
        var sale_mx = doc.createElement("sale_mx")
        root.appendChild(sale_mx)
        while (res_sale_mx.next()) {
            var rq = res_sale_mx.getString("rq")
            var tm = res_sale_mx.getString("tm")
            var sl = res_sale_mx.getString("sl")
            var zq = res_sale_mx.getString("zq")
            var je = res_sale_mx.getString("je")
            var element = doc.createElement("sale_mx")
            element.setAttribute("rq", rq)
            element.setAttribute("tm", tm)
            element.setAttribute("sl", sl)
            element.setAttribute("zq", zq)
            element.setAttribute("je", je)
            sale_mx.appendChild(element)
        }

        var res_sale_db = statement.executeQuery("select rq,sl,je from sale_db where date(rq)='${format.format(now)}'")
        var sale_db = doc.createElement("sale_db")
        root.appendChild(sale_db)
        while (res_sale_db.next()) {
            var rq = res_sale_db.getString("rq")
            var sl = res_sale_db.getString("sl")
            var je = res_sale_db.getString("je")
            var element = doc.createElement("sale_db")
            element.setAttribute("rq", rq)
            element.setAttribute("sl", sl)
            element.setAttribute("je", je)
            sale_db.appendChild(element)
        }
        res_goods.close()
        statement.close()
        connection.close()

        var stream = ByteArrayOutputStream()
        var tans = TransformerFactory.newInstance().newTransformer()
        tans.transform(DOMSource(doc), StreamResult(stream))
        var content = stream.toString()
        stream.close()
        return stream.toString()
    }
}