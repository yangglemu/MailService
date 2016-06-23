package com.soft.guoni

import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.sql.DriverManager
import java.util.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.system.exitProcess

/**
 * Created by 123456 on 2016/6/22.
 */

class MyFrame(title: String) : JFrame(title) {
    var timer: Timer = Timer()
    val email: Email by lazy { Email(DriverManager.getConnection(url)) }

    lateinit var path: String
    lateinit var trayIcon: TrayIcon
    var minuteCount: Int = 0

    companion object {
        val log = Log.log
        val minute = 1000 * 60
        val url = "jdbc:mysql://pc201408020832:3306/duobao?user=root&password=yuanbo960502"
    }

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        val quit = Button("Exit-退出程序")
        quit.size = Dimension(100, 35)
        val delete = Button("Clean-清理过时邮件")
        delete.size = Dimension(100, 35)
        val start = Button("(re)Start-(重新)启动工作线程")
        start.size = Dimension(100, 35)
        panel.add(Box.createVerticalGlue())
        panel.add(delete)
        panel.add(Box.createVerticalGlue())
        panel.add(start)
        panel.add(Box.createVerticalGlue())
        panel.add(quit)
        panel.add(Box.createVerticalGlue())
        delete.addActionListener { clean() }
        start.addActionListener { }
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

    private fun clean() {

    }

    private fun createConfig() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File("D:/D-drive-33671/duobao/res/config.xml"))
        val root = doc.documentElement
        val icon = root.getElementsByTagName("icon").item(0)
        val minutes = root.getElementsByTagName("minutes").item(0)
        this.minuteCount = minutes.textContent.toInt()
        this.path = icon.textContent
    }

    private fun createTray() {
        createConfig()
        val img = toolkit.createImage(path)
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
        trayIcon = TrayIcon(img, "数据发送中，请勿退出！", menu)
        SystemTray.getSystemTray().add(trayIcon)
    }

    fun start() {
        val delay: Long = 0
        val times: Long = 30 * 1000 * 60
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                email.postMsg()
                trayIcon.displayMessage("[${ Date().toString("yyyy-MM-dd HH:mm:ss") }]，最新数据发送成功！", "阳光服饰", TrayIcon.MessageType.INFO)
            }
        }, delay, times)
    }

    fun end() {
        timer.cancel()
        email.connection.close()
        exitProcess(0)
    }

}