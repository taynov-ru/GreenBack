package ru.taynov.tgbot.enums

enum class ModuleError(val text: String) {
    NOT_FOUND("Не найдено"),
    INTERNAL_ERROR("Ошибка сервера"),
    BEFORE_SELECT_DEVICE("Сначала выбери устройство"),
    UNKNOWN_DEVICE("Неизвестное устройство");

    fun getException() = TgBotException(text)

    open class TgBotException(message: String) : RuntimeException(message)
}

