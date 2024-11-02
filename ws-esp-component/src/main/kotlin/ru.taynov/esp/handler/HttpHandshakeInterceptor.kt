package ru.taynov.esp.handler

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class HttpHandshakeInterceptor : HandshakeInterceptor {

    private val logger = LoggerFactory.getLogger(HttpHandshakeInterceptor::class.java)

    companion object {
        const val MDC_CLIENT_IP = "clientIp"
        const val MDC_SESSION_ID = "sessionId"
        const val MDC_HEADERS = "headers"
    }

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val clientIp = request.remoteAddress.address?.hostAddress ?: "unknown"
        val sessionId = request.headers["Sec-WebSocket-Key"]?.firstOrNull() ?: "unknown"

        MDC.put(MDC_CLIENT_IP, clientIp)
        MDC.put(MDC_SESSION_ID, sessionId)

        addHeadersToMDC(request)

        logger.info("Starting WebSocket handshake")

        clearMDC(listOf(MDC_CLIENT_IP, MDC_SESSION_ID))

        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        addHeadersToMDC(request)

        if (exception != null) {
            logger.error("WebSocket handshake failed", exception)
        } else {
            logger.info("WebSocket handshake completed successfully")
        }

        clearMDC()
    }

    private fun addHeadersToMDC(request: ServerHttpRequest) {
        val headers = request.headers.entries.joinToString(", ") { (key, value) -> "$key: ${value.joinToString()}" }
        MDC.put(MDC_HEADERS, headers)
    }

    private fun clearMDC(clear: List<String> = emptyList()) {
        MDC.remove(MDC_CLIENT_IP)
        MDC.remove(MDC_SESSION_ID)
        MDC.remove(MDC_HEADERS)
        clear.forEach { MDC.remove(it) }
    }
}
