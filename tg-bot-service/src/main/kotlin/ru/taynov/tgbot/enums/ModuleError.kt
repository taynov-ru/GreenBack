package ru.taynov.tgbot.enums

enum class ModuleError(val text: String) {
    INTERNAL_ERROR("Ошибка сервера"),
    BEFORE_SELECT_DEVICE("Сначала выбери устройство"),
    PARAMETER_NOT_FOUND("Параметр не найден"),
    VALUE_INCORRECT("Значение некорректно"),
    DEVICE_IS_OFFLINE( "Устройство недоступно"),
    UNKNOWN_DEVICE("Неизвестное устройство");

    fun getException() = TgBotException(text)
    fun getException(message: String) = TgBotException(message)

    open class TgBotException(message: String) : RuntimeException(message)
}

