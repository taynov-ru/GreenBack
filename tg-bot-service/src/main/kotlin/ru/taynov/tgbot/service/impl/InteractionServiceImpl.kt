package ru.taynov.tgbot.service.impl

import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.service.EspInteractionService
import ru.taynov.tgbot.service.InteractionService

@Service
class InteractionServiceImpl(
    val service: EspInteractionService
) : InteractionService {
    override fun sendMessage(device: String, message: SetParamsResponse) {
        service.sendMessage(device, message)
    }

    override fun getLast(device: String): EspMessage? {
        return service.getLast(device)
    }

    override fun getDevices(): List<String> {
        return service.getDevices()
    }
}