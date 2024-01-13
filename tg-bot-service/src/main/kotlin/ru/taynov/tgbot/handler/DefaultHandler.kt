package ru.taynov.tgbot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.OperateResultDto

@Component
class DefaultHandler: MessageHandler {

    override fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto? {

        return OperateResultDto(
            SendMessage().apply {
                this.chatId = chatId
                this.text = "Неизвестная команда"
            })
    }

}