package ru.taynov.tgbot.handler

import org.telegram.telegrambots.meta.api.objects.Message
import ru.taynov.tgbot.callback.ParsedCallback
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.OperateResultDto

interface MessageHandler {
    fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto?
    fun operateCallback(chatId: String, parsedCallback: ParsedCallback, message: Message): OperateResultDto? {
        return null
    }
}