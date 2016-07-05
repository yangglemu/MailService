package com.soft.guoni

import javax.swing.SwingUtilities

/**
 * Created by 123456 on 2016/6/20.
 */
val log = Log.log
var sendCount = 1
fun main(args: Array<String>) {
    Class.forName("com.mysql.jdbc.Driver")
    log.fine("\r\n")
    log.info("进入邮件自动发送程序!")
    val f = MyFrame("邮件定时发送清理")
    f.isVisible = true
    SwingUtilities.invokeLater { f.isVisible = false }
}