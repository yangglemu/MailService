package com.soft.guoni

import javax.swing.SwingUtilities

/**
 * Created by 123456 on 2016/6/20.
 */


fun main(args: Array<String>) {
    Class.forName("com.mysql.jdbc.Driver")
    val f = MyFrame("邮件定时发送清理")
    f.isVisible = true
    SwingUtilities.invokeLater { f.isVisible = false }
}