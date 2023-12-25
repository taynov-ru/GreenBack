package ru.taynov.tgbot.service

import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.model.EspMessage

interface InteractionService {
    fun sendMessage(device: String, message: SetParamsResponse)
    fun getLast(device: String): EspMessage?
    fun getDevices(): List<String>
}