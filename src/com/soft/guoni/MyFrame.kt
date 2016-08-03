package com.soft.guoni

import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.Timer
import javax.swing.*
import kotlin.system.exitProcess

/**
 * Created by 123456 on 2016/6/22.
 */

class MyFrame(title: String) : JFrame(title) {
    var timer: Timer = Timer("myTimer_Main")
    val email: Email by lazy { Email(DriverManager.getConnection(url)) }
    val textArea = TextArea()
    lateinit var trayIcon: TrayIcon

    companion object {
        val url = "jdbc:mysql://pc201408020832:3306/duobao?user=root&password=yuanbo960502"
    }

    init {
        val panel = JPanel()
        val bl = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.layout = bl
        val content = Button("Content—获取发送内容")
        content.addActionListener {
            val sql = JOptionPane.showInputDialog(this@MyFrame, "日期格式: 2016-07-01")
            try {
                val formatter = SimpleDateFormat("yyyy-MM-dd")
                val date = formatter.parse(sql)
                textArea.text = email.document2String(date)
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(this@MyFrame, "输入格式,或者其它错误!")
                log.warning(e.message)
            }
        }
        val send = Button("Send—选择日期发送")
        send.addActionListener {
            val timer = Timer("myTimer_Temp")
            timer.schedule(object : java.util.TimerTask() {
                override fun run() {
                    val sql = JOptionPane.showInputDialog(this@MyFrame, "日期格式: 2016-07-01")
                    try {
                        val formatter = SimpleDateFormat("yyyy-MM-dd")
                        val date = formatter.parse(sql)
                        email.postMsg(date)
                    } catch (e: Exception) {
                        JOptionPane.showMessageDialog(this@MyFrame, "输入格式,或者其它错误!")
                        log.warning(e.message)
                    }
                }
            }, 0)
        }
        val quit = Button("Exit—退出程序")
        quit.addActionListener { end() }
        panel.add(textArea)

        val p0 = JPanel()
        panel.add(Box.createVerticalStrut(20))
        p0.layout = BoxLayout(p0, BoxLayout.X_AXIS)
        p0.add(Box.createHorizontalStrut(180))
        p0.add(content)
        p0.add(Box.createHorizontalStrut(180))
        panel.add(p0)

        val p1 = JPanel()
        panel.add(Box.createVerticalStrut(20))
        p1.layout = BoxLayout(p1, BoxLayout.X_AXIS)
        p1.add(Box.createHorizontalStrut(180))
        p1.add(send)
        p1.add(Box.createHorizontalStrut(180))
        panel.add(p1)
        val p = JPanel()
        panel.add(Box.createVerticalStrut(20))
        p.layout = BoxLayout(p, BoxLayout.X_AXIS)
        p.add(Box.createHorizontalStrut(180))
        p.add(quit)
        p.add(Box.createHorizontalStrut(180))
        panel.add(p)
        panel.add(Box.createVerticalStrut(20))
        contentPane = panel
        size = Dimension(600, 420)
        defaultCloseOperation = HIDE_ON_CLOSE
        this.addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                setLocationRelativeTo(null)
                val calendar = Calendar.getInstance(Locale.CHINA)
                val date = Date()
                calendar.time = date
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                var sDate = calendar.time.toString("yyyy-MM-dd")
                sDate += " 23:59:59"
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                try {
                    email.postMsg(sdf.parse(sDate))
                    log.info("第 0 封邮件发送成功!")
                    trayIcon.displayMessage("阳光服饰", "第 0 封邮件发送成功!", TrayIcon.MessageType.NONE)
                } catch (e: Exception) {
                    log.warning("第 0 封邮件发送错误,${e.message}")
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    e.printStackTrace(pw)
                    log.warning("${sw.toString()}")
                    pw.close()
                    trayIcon.displayMessage("阳光服饰", "第 0 封邮件发送失败!", TrayIcon.MessageType.ERROR)
                }
                start()
            }

            override fun windowIconified(e: WindowEvent) {
                isVisible = false
            }
        })
        createTray()
    }

    private fun createTray() {
        val path = javaClass.getResource("/res/Flower.png")
        val image = toolkit.createImage(path)
        this.iconImage = image
        val menu = PopupMenu()
        val normal = MenuItem("显示")
        normal.addActionListener {
            this.state = JFrame.NORMAL
            isVisible = true
        }
        menu.add(normal)
        val exit = MenuItem("退出")
        exit.addActionListener {
            end()
        }
        menu.add(exit)
        trayIcon = TrayIcon(image, "数据发送中, 请勿退出！", menu)
        trayIcon.isImageAutoSize = true
        SystemTray.getSystemTray().add(trayIcon)
    }

    fun start() {
        val delay: Long = 30L * 1000L        //  半分钟
        val times: Long = 30L * 1000 * 60    //  半小时
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                val count = com.soft.guoni.sendCount++
                try {
                    val date = Date()
                    email.postMsg(date)
                    val text = "[${date.toString("yyyy-MM-dd HH:mm:ss")}], 第 $count 封邮件发送成功!"
                    log.info("*第 $count 封邮件发送成功!")
                    trayIcon.displayMessage("阳光服饰", text, TrayIcon.MessageType.NONE)
                } catch(e: Exception) {
                    log.warning("第 $count 封邮件发送错误,${e.message}")
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    e.printStackTrace(pw)
                    log.warning("${sw.toString()}")
                    pw.close()
                    trayIcon.displayMessage("阳光服饰", "第 $count 封邮件发送失败!", TrayIcon.MessageType.ERROR)
                } finally {

                }
            }
        }, delay, times)
    }

    fun end() {
        timer.cancel()
        email.connection.close()
        log.info("退出邮件自动发送程序!")
        exitProcess(0)
    }

}