package ru.taynov.tgbot

import mu.KotlinLogging
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.taynov.tgbot.config.BotConfig

@Component
class TelegramBot(
    private val config: BotConfig
) : TelegramLongPollingBot(
    config.token
) {
    private val log = KotlinLogging.logger {}

    @EventListener(*[ContextRefreshedEvent::class])
    fun init() {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        try {
            telegramBotsApi.registerBot(this)
        } catch (e: TelegramApiException) {
            log.error("Error occurred: " + e.message)
        }
    }

    override fun getBotUsername(): String {
        return config.name
    }

    override fun onUpdateReceived(update: Update) {
        log.info("new update")
        if (update.hasMessage()) {
            val chatId = update.message.chatId.toString()
            val username = update.message.chat.userName
            val message = update.message.text
            execute(SendMessage(chatId, "echo: $message"))
            log.info("New message from $username: $message")
        }
    }
}