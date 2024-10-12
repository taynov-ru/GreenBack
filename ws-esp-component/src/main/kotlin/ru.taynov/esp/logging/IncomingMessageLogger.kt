package ru.taynov.esp.logging

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Service

@Service
class IncomingMessageLogger(
    private val logger: KLogger = KotlinLogging.logger {},
    private val objectMapper: ObjectMapper,
) {

    fun log(message: Any) {
        logger.info("New message from device: ${objectMapper.writeValueAsString(message)}")
    }

}