package com.soft.guoni

import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by 123456 on 2016/6/22.
 */
class Log {
    companion object {
        val log: Logger by lazy {
           val myLog = Logger.getLogger("myLog")
            myLog.level= Level.ALL
            myLog.addHandler(FileHandler("myLog.log"))
            myLog
        }
    }
}
