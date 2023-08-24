package com.github.senocak.util

import ch.qos.logback.classic.Level
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AppConstants {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    val corePoolSize: Int = Runtime.getRuntime().availableProcessors()
    const val TOKEN_HEADER_NAME = "Authorization"
    const val TOKEN_PREFIX = "Bearer "
    const val ADMIN = "ADMIN"
    const val USER = "USER"

    /**
     * Logging.
     */
    fun setLevel(loglevel: String) {
        val getLogger: ch.qos.logback.classic.Logger = getLogger()
        getLogger.level = Level.toLevel(loglevel)
        println("Logging level: ${getLogger.level}")
        log.debug("This is a debug message.")
        log.info("This is an info message.")
        log.warn("This is a warn message.")
        log.error("This is an error message.")
    }

    /**
     * Get logger.
     * @return -- logger
     */
    fun getLogger(): ch.qos.logback.classic.Logger =
        LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
}