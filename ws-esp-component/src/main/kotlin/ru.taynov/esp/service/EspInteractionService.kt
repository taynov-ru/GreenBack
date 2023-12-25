package ru.taynov.esp.service

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import ru.taynov.esp.dto.SetParamsResponse
import ru.taynov.esp.entity.toEntity
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.model.toModel
import ru.taynov.esp.repository.EspDataRepository

@Service
class EspInteractionService(
    val repository: EspDataRepository,
    val messagingTemplate: SimpMessagingTemplate,
) {
    fun sendMessage(device: String, message: SetParamsResponse) {
        messagingTemplate.convertAndSend("/topic/messages/$device", message)
    }

    fun saveMessage(message: EspMessage) {
        repository.save(message.toEntity())
    }

    fun getLast(device: String): EspMessage? {
        return repository.findDistinctById(device)?.toModel()
    }

    fun getDevices(): List<String> =
        repository.findAll().map { it.id }.distinct()

}