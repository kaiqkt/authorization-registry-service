package com.kaiqkt.services.authregistryservice.resources.exceptions

import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class UnexpectedException(private val errorMessage: String) : Exception() {

    private val loggedMessage = "Unexpected resource exception occurred at (${sourceLocation()}): $errorMessage"

    init {
        logger.error(loggedMessage)
    }

    private fun sourceLocation(): String? {
        val stackElement = stackTrace.firstOrNull()

        return when{
            stackElement?.fileName == null -> "Unknown source"
            stackElement.lineNumber < 0 -> stackElement.fileName
            else -> (stackElement.fileName?.plus(":") ?: "") + stackElement.lineNumber
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}