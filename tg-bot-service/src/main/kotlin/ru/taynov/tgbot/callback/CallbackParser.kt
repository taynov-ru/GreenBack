package ru.taynov.tgbot.callback

import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.taynov.tgbot.callback.ParsedCallback.ParameterEnum

@Component
class CallbackParser {

    private val log: KLogger = KotlinLogging.logger {}

    fun getParsedCallback(text: String?): ParsedCallback {
        var trimText = ""
        if (text != null) {
            trimText = text.trim { it <= ' ' }
        }
        val callbackAndText = getDelimitedCallbackFromText(trimText)


        val parameters =
            runCatching { Json.decodeFromString(callbackAndText.second) as Map<String, String?> }.getOrNull()
                ?: emptyMap()

        return ParsedCallback(getCallbackFromText(callbackAndText.first)).apply {
            parameters.entries.forEach { setParameter(ParameterEnum.valueOf(it.key), it.value) }
        }
    }

    private fun getCallbackFromText(text: String): Callback {
        val result = Callback.entries.firstOrNull { text == it.name } ?: Callback.UNKNOWN
        if (result == Callback.UNKNOWN) log.debug("Can't parse callback: $text")
        return result
    }

    private fun getDelimitedCallbackFromText(trimText: String): Pair<String, String> {
        val callbackText = if (trimText.contains(DELIMITER_CALLBACK)) {
            val indexOfDelimiter = trimText.indexOf(DELIMITER_CALLBACK)
            Pair(trimText.substring(0, indexOfDelimiter), trimText.substring(indexOfDelimiter + 1))
        } else Pair(trimText, "")
        return callbackText
    }

    companion object {
        private const val DELIMITER_CALLBACK = "|"
    }
}