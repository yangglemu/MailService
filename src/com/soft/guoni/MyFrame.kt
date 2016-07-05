package com.soft.guoni

import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.sql.DriverManager
import java.util.*
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess

/**
 * Created by 123456 on 2016/6/22.
 */

class MyFrame(title: String) : JFrame(title) {
    var timer: Timer = Timer()
    val email: Email by lazy { Email(DriverManager.getConnection(url)) }

    lateinit var trayIcon: TrayIcon

    companion object {
        val url = "jdbc:mysql://pc201408020832:3306/duobao?user=root&password=yuanbo960502"
    }

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        val quit = Button("Exit-退出程序")
        quit.size = Dimension(100, 35)
        panel.add(quit)
        quit.addActionListener { end() }

        contentPane = panel
        size = Dimension(400, 200)
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
        //image = image.getScaledInstance(50, 50, Image.SCALE_DEFAULT)
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
                    e.printStackTrace()
                    trayIcon.displayMessage("阳光服饰", "第 ${com.soft.guoni.sendCount} 封邮件发送失败!", TrayIcon.MessageType.NONE)
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