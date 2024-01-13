package ru.taynov.tgbot.dto

import org.telegram.telegrambots.meta.api.methods.BotApiMethod

data class OperateResultDto(
    val result: BotApiMethod<*>
)

fun BotApiMethod<*>.toOperateResult(): OperateResultDto {
    return OperateResultDto(this)
}