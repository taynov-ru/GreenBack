package ru.taynov.tgbot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import ru.taynov.tgbot.command.Command
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.OperateResultDto
import ru.taynov.tgbot.dto.toOperateResult
import ru.taynov.tgbot.enums.State
import ru.taynov.tgbot.service.UserService

@Component
class SystemHandler(
    private val userService: UserService
) : MessageHandler {

    override fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto? {
        return when (parsedCommand.command) {
            Command.START -> getMessageStart(chatId, message)
            Command.HELP -> getMessageHelp(chatId)
            else -> null
        }?.toOperateResult()
    }

    private fun getMessageHelp(chatId: String): SendMessage {
        return SendMessage().apply {
            this.chatId = chatId
            this.text =
                """
                Автор бота @taynov.
                """.trimIndent()
        }
    }

    private fun getMessageStart(chatId: String, message: Message): SendMessage {
        val firstname = message.from.firstName
        val username = message.from.userName
        userService.setState(chatId, State.NONE, username)

        return SendMessage().apply {
            this.chatId = chatId
            this.text =
                """
                Привет, $firstname! Воспользуйся списком команд.
                """.trimIndent()
        }
    }

}