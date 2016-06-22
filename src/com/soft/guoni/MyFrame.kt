package com.soft.guoni

import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.sql.DriverManager
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*
import kotlin.system.exitProcess

/**
 * Created by 123456 on 2016/6/22.
 */

class MyFrame(title: String) : JFrame(title) {
    val minute = 1000 * 60
    val url = "jdbc:mysql://pc201408020832:3306/duobao?user=root&password=yuanbo960502"
    var timer: Timer? = null
    val email: Email by lazy { Email(DriverManager.getConnection(url)) }
    val log = Logger.getLogger("sunshine")

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        val quit = Button("Exit-退出程序")
        val delete = Button("Clean-清理过时邮件")
        val start = Button("(re)Start-(重新)启动工作线程")
        panel.add(Box.createVerticalGlue())
        panel.add(delete)
        panel.add(Box.createVerticalGlue())
        panel.add(start)
        panel.add(Box.createVerticalGlue())
        panel.add(quit)
        panel.add(Box.createVerticalGlue())
        delete.addActionListener { clean() }
        start.addActionListener { start(30) }
        quit.addActionListener { timer?.stop(); exitProcess(0); }
        contentPane = panel
        pack()
        size = Dimension(400, 200)
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        this.addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent) {
                setLocationRelativeTo(null)
                start(30)
            }

            override fun windowIconified(e: WindowEvent) {
                isVisible = false
            }
        })
        createTray()
        log.level = Level.WARNING
        val handler = ConsoleHandler()
        handler.level = Level.ALL
        log.addHandler(handler)
    }

    private fun clean() {

    }

    private fun createTray() {
        val img=toolkit.createImage("./images/icon.png")
        val trayIcon = TrayIcon(img)
        val menu = PopupMenu()
        val normal = MenuItem("显示")
        normal.addActionListener {
            this.state = JFrame.NORMAL
            isVisible = true
        }
        menu.add(normal)
        val exit = MenuItem("退出")
        exit.addActionListener { exitProcess(0) }
        menu.add(exit)
        trayIcon.popupMenu = menu
        SystemTray.getSystemTray().add(trayIcon)
    }

    fun start(minutes: Int) {
        if (timer == null) {
            timer = Timer(minutes * minute, {
                email.postMsg()
            })
            timer?.start()
        } else {
            timer?.restart()
        }
    }
}