package ru.taynov.esp.enums

import kotlin.reflect.KClass

enum class ParamName(
    val value: String,
    val type: KClass<*>,
) {
    AUTO_CONTROL_HEAT("Автоматическое управление отоплением 🔘", Boolean::class),
    CHECKING_VOLTAGE_ENABLED("Проверка наличия питания 🔌", Boolean::class),
    HEATING_ENABLED("Насос отопления 💧", Boolean::class),
    HIGH_BOUND_ALARM_TEMPERATURE("Макс. температура сигнала 🔊", Int::class),
    HIGH_BOUND_HEAT_TEMPERATURE("Темп. выключения отопления 💧", Int::class),
    LOW_BOUND_ALARM_TEMPERATURE("Мин. температура сигнала 🔊", Int::class),
    LOW_BOUND_HEAT_TEMPERATURE("Темп. включения отопления 💧", Int::class),
    ALARM_LOUD_MODE_ENABLED("Звуковое оповещение 🔔", Boolean::class),
    WINDOW_MODE("Форточка", WindowMode::class),
    AUTO_CONTROL_WINDOW("Автоматическое управление форточкой", Boolean::class),
    CLOSE_WINDOW_TEMPERATURE("Темп. закрытия форточки", Int::class),
    OPEN_WINDOW_TEMPERATURE("Темп. открытия форточки", Int::class),
}

enum class WindowMode(
    val value: String
) {
    CLOSED("Закрыто"),
    AJAR("Приоткрыто"),
    OPENED("Открыто"),
    ;

    companion object {
        fun valueFromOrdinal(ordinal: Int): WindowMode {
            return entries.getOrNull(ordinal) ?: CLOSED
        }
    }
}
