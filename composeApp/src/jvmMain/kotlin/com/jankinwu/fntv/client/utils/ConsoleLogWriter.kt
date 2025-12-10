package com.jankinwu.fntv.client.utils

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsoleLogWriter : LogWriter() {
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val timestamp = LocalDateTime.now().format(timeFormatter)
        val logMessage = "[$timestamp] [$tag] [${severity.name}] $message"
        if (severity == Severity.Error || severity == Severity.Assert) {
            System.err.println(logMessage)
            throwable?.printStackTrace()
        } else {
            println(logMessage)
            throwable?.printStackTrace()
        }
    }
}
