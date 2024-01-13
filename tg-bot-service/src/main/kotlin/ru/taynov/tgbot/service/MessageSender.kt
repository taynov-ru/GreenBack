package ru.taynov.tgbot.service

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendSticker
import org.telegram.telegrambots.meta.api.objects.Message
import ru.taynov.tgbot.TelegramBot

@Controller
class MessageSender(
    private val bot: TelegramBot,
) {

    private val log: KLogger = KotlinLogging.logger { }


    @OptIn(DelicateCoroutinesApi::class)
    val globalScopeReporter = GlobalScope.launch {
        while (true) {
            try {
                val method = bot.sendQueue.poll() ?: continue
                log.debug("Get new msg to send {}", method)
                send(method)
            } catch (e: Exception) {
                log.error(e) {}
            }
            delay(300)
        }
    }

    private fun send(method: Any) {
        when (messageType(method)) {
            MessageType.EXECUTE -> {
                val message: BotApiMethod<Message> = method as BotApiMethod<Message>
                log.debug("Use Execute for {}", message)
                bot.execute(message)
            }

            else -> log.warn("Cant detect type of object. $method")
        }
    }

    private fun messageType(message: Any): MessageType {
        if (message is SendSticker) return MessageType.STICKER
        return if (message is BotApiMethod<*>) MessageType.EXECUTE else MessageType.NOT_DETECTED
    }

    internal enum class MessageType {
        EXECUTE,
        STICKER,
        NOT_DETECTED
    }

}