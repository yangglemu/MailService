package com.soft.guoni

import java.io.File
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

/**
 * Created by 123456 on 2016/6/22.
 */
class Log {
    companion object {
        val log: Logger by lazy {
            val myLog = Logger.getLogger("myLog")
            myLog.level = Level.ALL
            var path = System.getProperty("user.dir")
            path += "\\myLog"
            val file = File(path)
            if (!file.exists()) file.mkdir()
            path = "$path\\MailService${Date().toString("yyyyMM")}.log"
            val fh = FileHandler(path, true)
            fh.level = Level.ALL
            fh.formatter = object : Formatter() {
                override fun format(record: LogRecord?): String {
                    val r = record!!
                    if (r.level == Level.FINE) {
                        return r.message
                    } else {
                        val date = Date().toString("yyyy-MM-dd HH:mm:ss")
                        val value = date + "  " + r.level.name.substring(0, 4) + ": " + r.message + "\r\n"
                        return value
                    }
                }
            }
            myLog.addHandler(fh)
            myLog
        }
    }
}
