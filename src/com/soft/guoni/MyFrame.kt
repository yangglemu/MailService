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

    lateinit var trayIcon: TrayIcon

    companion object {
        val url = "jdbc:mysql://pc201408020832:3306/duobao?user=root&password=yuanbo960502"
    }

    init {
        val panel = JPanel()
        val bl = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.layout = bl
        val send = Button("Send—选择日期")
        send.addActionListener {
            val timer = Timer("myTimer_Temp")
            timer.schedule(object : java.util.TimerTask() {
                override fun run() {
                    val sql = JOptionPane.showInputDialog(this@MyFrame, "日期格式: 2000-01-01")
                    try {
                        val formatter = SimpleDateFormat("yyyy-MM-dd")
                        val date = formatter.parse(sql)
                        email.postMsg(date)
                    } catch (e: Exception) {
                        JOptionPane.showMessageDialog(this@MyFrame, "选择日期发送出错，检查输入格式！")
                        log.warning(e.message)
                    }
                }
            }, 0)
        }
        val quit = Button("Exit—退出程序")
        quit.addActionListener { end() }
        val p1 = JPanel()
        panel.add(Box.createVerticalStrut(120))
        p1.layout = BoxLayout(p1, BoxLayout.X_AXIS)
        p1.add(Box.createHorizontalStrut(180))
        p1.add(send)
        p1.add(Box.createHorizontalStrut(180))
        panel.add(p1)
        val p = JPanel()
        panel.add(Box.createVerticalStrut(60))
        p.layout = BoxLayout(p, BoxLayout.X_AXIS)
        p.add(Box.createHorizontalStrut(180))
        p.add(quit)
        p.add(Box.createHorizontalStrut(180))
        panel.add(p)
        panel.add(Box.createVerticalStrut(120))
        contentPane = panel
        size = Dimension(560, 420)
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        this.addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                setLocationRelativeTo(null)
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
        var image = toolkit.createImage(path)
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
        val delay: Long = 0
        val times: Long = 30 * 1000 * 60
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                val count = com.soft.guoni.sendCount++
                try {
                    email.postMsg()
                    val text = "[${Date().toString("yyyy-MM-dd HH:mm:ss")}], 第 $count 封邮件发送成功!"
                    log.info("*第 $count 封邮件发送成功!")
                    trayIcon.displayMessage("阳光服饰", text, TrayIcon.MessageType.NONE)
                } catch(e: Exception) {
                    log.warning("第 $count 封邮件发送错误,${e.message}")
                    val sw = StringWriter()
                    val pw = PrintWriter(sw)
                    e.printStackTrace(pw)
                    log.warning("${sw.toString()}")
                    trayIcon.displayMessage("阳光服饰", "第 $count 封邮件发送失败!", TrayIcon.MessageType.ERROR)
                } finally {
                    log.fine("\r\n")
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