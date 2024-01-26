package ru.taynov.esp.handler

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import ru.taynov.esp.enums.DeviceStatus
import ru.taynov.esp.service.EspInteractionService
import ru.taynov.esp.service.OnlineDeviceService
import kotlin.math.log

@Component
class WebSocketEventListener(
    private val onlineDeviceService: OnlineDeviceService,
    private val espInteractionService: EspInteractionService,
) {

    @EventListener
    fun handleWebSocketConnectListener(event: SessionSubscribeEvent?) {

        val deviceId = event?.message?.headers?.get("simpDestination", String::class.java)?.substringAfterLast("/")
        if (deviceId == null) {
            logger.warn("DeviceId is null. Passing subscription event...")
            return
        }
        logger.info("DeviceId $deviceId subscribed")
        val sessionId = event.message.headers.get("simpSessionId", String::class.java)
        if (sessionId == null) {
            logger.warn("SessionId is null. Passing subscription event...")
            return
        }
        onlineDeviceService.add(sessionId, deviceId)
        espInteractionService.invokeDeviceStatusCallback(deviceId, DeviceStatus.ONLINE)
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val deviceId = onlineDeviceService.get(event.sessionId)
        logger.info("Disconnected deviceId: $deviceId")
        onlineDeviceService.delete(event.sessionId)
        deviceId?.let { espInteractionService.invokeDeviceStatusCallback(it, DeviceStatus.OFFLINE) }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)
    }
}