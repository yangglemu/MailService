package com.soft.guoni

import java.util.logging.Logger

/**
 * Created by 123456 on 2016/6/22.
 */
class Log {
    companion object {
        val log: Logger by lazy {
            System.setProperty("java.util.logging.config.file", "D:/D-drive-33671/duobao/res/log.properties")
            Logger.getLogger("myLogger")
        }
    }
}
