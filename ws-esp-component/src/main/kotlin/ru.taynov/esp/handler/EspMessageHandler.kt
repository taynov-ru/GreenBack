package ru.taynov.esp.handler

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.bind.annotation.RestController
import ru.taynov.esp.logging.IncomingMessageLogger
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.service.EspInteractionService


@RestController
class EspMessageHandler(
    val espInteractionService: EspInteractionService,
    val logger: IncomingMessageLogger
) {

    @MessageMapping("/server")
    fun putMessage(message: EspMessage) {
        logger.log(message)
        espInteractionService.saveMessage(message)
    }
}