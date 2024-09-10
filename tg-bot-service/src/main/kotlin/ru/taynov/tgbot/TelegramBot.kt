package ru.taynov.tgbot

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.taynov.tgbot.config.BotConfig


@Component
class TelegramBot(
    private val config: BotConfig
) : TelegramLongPollingBot(config.token) {

    private val log = KotlinLogging.logger {}

    private val sendQueue: Channel<PartialBotApiMethod<*>> = Channel(Channel.UNLIMITED)
    private val receiveQueue: Channel<Update> = Channel(Channel.UNLIMITED)

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

    fun getSendQueue() = sendQueue
    fun getReceiveQueue() = receiveQueue

    override fun onUpdateReceived(update: Update) {
        log.debug("Received update. updateID: " + update.updateId)
        runBlocking {
            getReceiveQueue().send(update)
        }
    }

    fun send(message: PartialBotApiMethod<*>) {
        runBlocking {
            getSendQueue().send(message)
        }
    }

}