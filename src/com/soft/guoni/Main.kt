package com.soft.guoni

/**
 * Created by 123456 on 2016/6/20.
 */

val hour = 1000 * 60 * 60

fun main(args: Array<String>) {
    Class.forName("com.mysql.jdbc.Driver")
    MyFrame("邮件定时发送清理").isVisible = true
}