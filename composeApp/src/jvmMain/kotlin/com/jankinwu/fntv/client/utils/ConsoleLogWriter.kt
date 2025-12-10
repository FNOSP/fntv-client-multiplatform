package com.jankinwu.fntv.client.utils

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsoleLogWriter : LogWriter() {
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val RESET = "\u001B[0m"
    private val RED = "\u001B[31m"
    private val GREEN = "\u001B[32m"
    private val YELLOW = "\u001B[33m"
    private val BLUE = "\u001B[34m"
    private val MAGENTA = "\u001B[35m"
    private val CYAN = "\u001B[36m"
    private val WHITE = "\u001B[37m"

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val timestamp = LocalDateTime.now().format(timeFormatter)
        
        val severityColor = when (severity) {
            Severity.Verbose -> WHITE
            Severity.Debug -> BLUE
            Severity.Info -> GREEN
            Severity.Warn -> YELLOW
            Severity.Error -> RED
            Severity.Assert -> RED
        }

        // 格式参考 Spring Boot: [Timestamp] [Severity] [Tag] Message
        // Timestamp: 默认/白色
        // Severity: 对应级别颜色
        // Tag: 青色
        // Message: 默认
        val logMessage = "$WHITE[$timestamp]$RESET $severityColor[${severity.name.padEnd(5)}]$RESET $CYAN[$tag]$RESET $message"
        
        if (severity == Severity.Error || severity == Severity.Assert) {
            System.err.println(logMessage)
            throwable?.printStackTrace()
        } else {
            println(logMessage)
            throwable?.printStackTrace()
        }
    }
}
