package com.soft.guoni

import javax.swing.SwingUtilities

/**
 * Created by 123456 on 2016/6/20.
 */
val log = Log.log
var sendCount = 0
fun main(args: Array<String>) {
    Class.forName("com.mysql.jdbc.Driver")
    log.info("程序启动!")
    val f = MyFrame("邮件定时发送清理")
    f.isVisible = true
    SwingUtilities.invokeLater { f.isVisible = false }
}