package ru.taynov.tgbot

import mu.KotlinLogging
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.taynov.tgbot.config.BotConfig
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


@Component
class TelegramBot(
    private val config: BotConfig
) : TelegramLongPollingBot(
    config.token
) {
    private val log = KotlinLogging.logger {}

    val sendQueue: Queue<BotApiMethod<*>> = LinkedBlockingQueue()
    val receiveQueue: Queue<Update> = LinkedBlockingQueue()

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
        log.debug("Received update. updateID: " + update.updateId)
        receiveQueue.add(update)
    }
}