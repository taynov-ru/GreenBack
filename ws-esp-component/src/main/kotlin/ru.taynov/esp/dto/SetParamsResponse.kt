package ru.taynov.esp.dto

import ru.taynov.esp.model.Param


data class SetParamsResponse(
    val params: Set<Param>
)