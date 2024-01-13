package ru.taynov.tgbot.validator

import org.springframework.stereotype.Component
import ru.taynov.tgbot.enums.ModuleError
import ru.taynov.tgbot.service.InteractionService

@Component
class DeviceValidator(
    private val interactionService: InteractionService
) {

    fun validateDeviceId(text: String) {
        val devices = interactionService.getDevices()
        if (!devices.contains(text)) throw ModuleError.UNKNOWN_DEVICE.getException()
    }

}