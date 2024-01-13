package ru.taynov.tgbot.handler

import org.telegram.telegrambots.meta.api.objects.Message
import ru.taynov.tgbot.callback.ParsedCallback
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.OperateResultDto
import ru.taynov.tgbot.state.ExtendedState

interface MessageHandler {
    fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto? {
        return null
    }

    fun operateCallback(chatId: String, parsedCallback: ParsedCallback, message: Message): OperateResultDto? {
        return null
    }

    fun operateMessage(chatId: String, extendedState: ExtendedState, message: Message): OperateResultDto? {
        return null
    }
}