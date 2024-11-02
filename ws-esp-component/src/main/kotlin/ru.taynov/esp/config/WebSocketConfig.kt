package ru.taynov.esp.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import ru.taynov.esp.handler.HttpHandshakeInterceptor
import ru.taynov.esp.handler.LoggingChannelInterceptor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val loggingChannelInterceptor: LoggingChannelInterceptor,
    private val httpHandshakeInterceptor: HttpHandshakeInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/server")
            .setAllowedOriginPatterns("*")
            .addInterceptors(httpHandshakeInterceptor)
            .withSockJS()

    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
            .enableSimpleBroker("/topic")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(loggingChannelInterceptor)
    }

    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration.interceptors(loggingChannelInterceptor)
    }
}
