package ru.taynov.esp.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import java.nio.charset.Charset

@Component
class LoggingChannelInterceptor(
    private val objectMapper: ObjectMapper
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(LoggingChannelInterceptor::class.java)

    companion object {
        const val MDC_MESSAGE_HEADERS = "messageHeaders"
        const val MDC_EXCEPTION = "exception"
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        MDC.put(MDC_MESSAGE_HEADERS, message.headers.toString())
        logger.info("Sending message: ${message.getJsonPayload()}")
        MDC.remove(MDC_MESSAGE_HEADERS)
        return message
    }

    override fun afterSendCompletion(message: Message<*>, channel: MessageChannel, sent: Boolean, ex: Exception?) {
        if (ex != null) {
            MDC.put(MDC_EXCEPTION, ex.message)
            logger.error("Message send error: ${message.getJsonPayload()}")
            MDC.remove(MDC_EXCEPTION)
        } else {
            logger.info("Message sent successfully: ${message.getJsonPayload()}")
        }
    }

    override fun postReceive(message: Message<*>, channel: MessageChannel): Message<*>? {
        MDC.put(MDC_MESSAGE_HEADERS, message.headers.toString())
        logger.info("Received message: ${message.getJsonPayload()}")
        MDC.remove(MDC_MESSAGE_HEADERS)
        return message
    }

    private fun Message<*>.getJsonPayload(): String {
        val json = objectMapper.writeValueAsString(this.payload).removeSurrounding("\"")
        return Base64Utils.decodeFromString(json).toString(Charset.defaultCharset())
    }
}

