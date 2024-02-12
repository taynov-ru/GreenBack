package ru.taynov.tgbot.monitoring

import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import ru.taynov.tgbot.TelegramBot
import java.io.File
import java.io.FileWriter
import javax.annotation.PostConstruct

@Service
class AlertingService(
    private val bot: TelegramBot,
    @Value("\${bot.admin_id}")
    val adminId: String
) {
    private val log: KLogger = KotlinLogging.logger { }

    @PostConstruct
    fun sendOnStartMessage() {
        bot.sendQueue.add(SendMessage().apply {
            chatId = adminId
            text = "Бот запущен"
        })
    }

    fun sendStacktrace(throwable: Throwable) {
        runCatching {
            val tempFile = File.createTempFile("throwable", ".txt")

            FileWriter(tempFile).use {
                it.write(throwable.stackTraceToString())
            }

            val sendDocument = SendDocument().apply {
                this.chatId = adminId
                this.document = InputFile(tempFile, tempFile.name)

            }
            bot.sendQueue.add(sendDocument)
        }.onFailure {
            log.warn(it) { "Ошибка при отправке stacktrace" }
        }

    }
}