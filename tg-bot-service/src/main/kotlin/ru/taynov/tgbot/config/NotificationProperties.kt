package ru.taynov.tgbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding


@ConstructorBinding
@ConfigurationProperties("notification")
data class NotificationProperties(
    val changedParameter: Boolean = true,
    val alarm: Boolean = true,
    val deviceStatus: Boolean = true,
)