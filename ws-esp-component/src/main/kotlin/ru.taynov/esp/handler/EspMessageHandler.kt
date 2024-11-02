package ru.taynov.esp.handler

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.RestController
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.service.MessageProcessor
import ru.taynov.esp.service.OnlineDeviceService


@RestController
class EspMessageHandler(
    private val messageProcessor: MessageProcessor,
    private val onlineDeviceService: OnlineDeviceService,
) {

    @MessageMapping("/server")
    fun putMessage(message: EspMessage) {
        onlineDeviceService.setOnline(message.id)
        messageProcessor.process(message)
    }
}