package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.tgbot.entity.UserStateEntity
import ru.taynov.tgbot.enums.State
import ru.taynov.tgbot.repository.UserStateRepository
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

@Service
class UserService(
    private val repository: UserStateRepository
) {

    fun getUser(chatId: String): UserStateEntity {
        return repository.findById(chatId.toLong()).getOrDefault(UserStateEntity(userId = chatId.toLong()))
    }

    fun setState(chatId: String, state: State, username: String? = null) {
        repository.findById(chatId.toLong()).getOrNull()?.also {
            it.state = state
            it.username = username
            repository.save(it)
        }
    }

    fun getState(chatId: String): State {
        return repository.findById(chatId.toLong()).map { it.state }.orElse(State.NONE)
    }
}