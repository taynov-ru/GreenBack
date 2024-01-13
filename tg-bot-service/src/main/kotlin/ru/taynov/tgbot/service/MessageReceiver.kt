package ru.taynov.tgbot.service

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
import ru.taynov.tgbot.enums.State
import ru.taynov.tgbot.handler.HandlerProvider


@Controller
class MessageReceiver(
    private val bot: TelegramBot,
    private val commandParser: CommandParser,
    private val callbackParser: CallbackParser,
    private val handlerProvider: HandlerProvider,
    private val userService: UserService,
) {
    private val log = KotlinLogging.logger {}

    @OptIn(DelicateCoroutinesApi::class)
    val globalScopeReporter = GlobalScope.launch {
        while (true) {
            try {
                val update = bot.receiveQueue.poll() ?: continue
                analyze(update)
            } catch (ex: Exception) {
                log.error(ex) { "Analyze error" }
            }
            delay(500)
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
        val parsedCommand = callbackParser.getParsedCallback(callback.data)
        clearState(chatId)
        val handler = handlerProvider.handlerForCallback(parsedCommand.callback)
        operateCatching(chatId) { handler.operateCallback(chatId, parsedCommand, callback.message) }
    }


    private fun analyzeMessage(message: Message) {
        val chatId = message.chatId.toString()
        log.info { "Message from chatId: $chatId" }
        val parsedCommand = commandParser.getParsedCommand(message.text)
        val handler =
            if (parsedCommand.command != Command.NONE) {
                clearState(chatId)
                handlerProvider.handlerForCommand(parsedCommand.command)
            } else
                handlerProvider.handlerByState(chatId)
        operateCatching(chatId) { handler.operateCommand(chatId, parsedCommand, message) }
    }

    private fun operateCatching(chatId: String, function: () -> OperateResultDto?) {
        runCatching { function() }
            .onSuccess {
                it?.result?.let { result ->
                    bot.sendQueue.add(result)
                }
            }
            .onFailure {
                bot.sendQueue.add(SendMessage().apply {
                    this.chatId = chatId
                    this.text =
                        if (it is ModuleError.TgBotException) it.message.toString()
                        else ModuleError.INTERNAL_ERROR.text
                })
            }
    }

    private fun clearState(chatId: String) {
        userService.setState(chatId, State.NONE)
    }

}