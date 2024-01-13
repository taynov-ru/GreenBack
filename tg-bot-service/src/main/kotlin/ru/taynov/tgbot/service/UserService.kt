package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.tgbot.entity.UserStateEntity
import ru.taynov.tgbot.repository.UserStateRepository
import ru.taynov.tgbot.state.ExtendedState
import ru.taynov.tgbot.state.State
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
        setState(chatId, ExtendedState(state), username)
    }

    fun setState(chatId: String, state: ExtendedState, username: String? = null) {
        getUser(chatId).also {
            it.state = state.state
            it.data = state.payload ?: ""
            if (username != null) it.username = username
            repository.save(it)
        }
    }

    fun getState(chatId: String): ExtendedState {
        return repository.findById(chatId.toLong()).map { ExtendedState(it.state, it.data.takeUnless(""::equals)) }.getOrNull()
            ?: ExtendedState(State.NONE, null)
    }
}