package ru.taynov.tgbot.service

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import ru.taynov.tgbot.TelegramBot
import ru.taynov.tgbot.callback.CallbackParser
import ru.taynov.tgbot.command.Command
import ru.taynov.tgbot.command.CommandParser
import ru.taynov.tgbot.dto.OperateResultDto
import ru.taynov.tgbot.enums.ModuleError
import ru.taynov.tgbot.handler.HandlerProvider
import ru.taynov.tgbot.monitoring.AlertingService
import ru.taynov.tgbot.state.State


@OptIn(DelicateCoroutinesApi::class)
@Controller
class MessageReceiver(
    private val bot: TelegramBot,
    private val commandParser: CommandParser,
    private val callbackParser: CallbackParser,
    private val handlerProvider: HandlerProvider,
    private val userService: UserService,
    private val alertingService: AlertingService,
) {
    private val log = KotlinLogging.logger {}

    init {
        GlobalScope.launch {
            for (update in bot.getReceiveQueue()) {
                analyze(update)
            }
        }
    }

    private fun analyze(update: Update) {
        if (update.hasMessage()) {
            log.debug("Message received: {}", update)
            analyzeMessage(update.message)
        }
        if (update.hasCallbackQuery()) {
            log.debug("Callback received: {}", update)
            analyzeCallback(update.callbackQuery)
        }
    }

    private fun analyzeCallback(callback: CallbackQuery) {
        val chatId = callback.message.chatId.toString()
        log.info { "Callback from chatId: $chatId" }
        operateCatching(chatId) {
            val parsedCommand = callbackParser.getParsedCallback(callback.data)
            clearState(chatId)
            val handler = handlerProvider.handlerForCallback(parsedCommand.callback)
            return@operateCatching handler.operateCallback(chatId, parsedCommand, callback.message)
        }
    }


    private fun analyzeMessage(message: Message) {
        val chatId = message.chatId.toString()
        log.info { "Message from chatId: $chatId" }
        operateCatching(chatId) {
            val parsedCommand = commandParser.getParsedCommand(message.text)
            return@operateCatching if (parsedCommand.command != Command.NONE) {
                clearState(chatId)
                val handler = handlerProvider.handlerForCommand(parsedCommand.command)
                handler.operateCommand(chatId, parsedCommand, message)
            } else {
                val extendedState = userService.getState(chatId)
                val handler = handlerProvider.handlerByState(extendedState)
                handler.operateMessage(chatId, extendedState, message)
            }
        }
    }

    private fun operateCatching(chatId: String, function: () -> OperateResultDto?) {
        runCatching { function() }
            .onSuccess {
                it?.result?.let { result ->
                    bot.send(result)
                }
            }
            .onFailure {
                bot.send(SendMessage().apply {
                    this.chatId = chatId
                    this.text =
                        if (it is ModuleError.TgBotException) it.message.toString()
                        else {
                            log.warn(it) { "ChatId error $chatId" }
                            ModuleError.INTERNAL_ERROR.text
                        }
                })
                if (it !is ModuleError.TgBotException) {
                    alertingService.sendStacktrace(it)
                }
            }
    }


    private fun clearState(chatId: String) {
        userService.setState(chatId, State.NONE)
    }

}