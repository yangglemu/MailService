package com.soft.guoni

import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.nimbus.NimbusLookAndFeel

/**
 * Created by 123456 on 2016/6/20.
 */
val log = Log.log
var sendCount = 1
fun main(args: Array<String>) {
    Class.forName("com.mysql.jdbc.Driver")
    UIManager.setLookAndFeel(NimbusLookAndFeel())
    log.info("进入邮件自动发送程序!")
    val f = MyFrame("定时发送数据")
    f.isVisible = true
    SwingUtilities.invokeLater { f.isVisible = false }
}