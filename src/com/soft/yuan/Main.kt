package com.soft.yuan

/**
 * Created by 123456 on 2016/6/20.
 */

fun main(args:Array<String>){
    Class.forName("com.mysql.jdbc.Driver")

    var mail = Mail()
    var xml=mail.getXmlContent()
    println(xml)
}