package ru.taynov.tgbot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "bot")
class BotConfig(
    @Value("\${bot.name}")
    val name: String,
    @Value("\${bot.token}")
    val token: String,
)