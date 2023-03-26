package com.zwendo.restrikt.plugin

import java.nio.file.FileAlreadyExistsException
import java.time.LocalDateTime
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.createFile
import kotlin.io.path.writeText

private const val LOG_FILE = "/Users/lorris/Desktop/restrikt.log"

internal interface Logger {

    fun log(level: Level, message: Any?)

    fun debug(message: Any?) = log(Level.DEBUG, message)

    fun info(message: Any?) = log(Level.INFO, message)

    fun warn(message: Any?) = log(Level.WARN, message)

    fun error(message: Any?) = log(Level.ERROR, message)

    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }

    companion object Default : Logger {

        private val file = try {
            Path(LOG_FILE).createFile()
        } catch (e: FileAlreadyExistsException) {
            Path(LOG_FILE)
        }.also { it.writeText("") }

        override fun log(level: Level, message: Any?) =
            file.appendText("${LocalDateTime.now()} [$level] $message\n")

    }



}

