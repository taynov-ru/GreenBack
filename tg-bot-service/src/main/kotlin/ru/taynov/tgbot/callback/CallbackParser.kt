package ru.taynov.tgbot.callback

import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class CallbackParser {

    private val log: KLogger = KotlinLogging.logger {}

    fun getParsedCallback(text: String?): ParsedCallback {
        var trimText = ""
        if (text != null) {
            trimText = text.trim { it <= ' ' }
        }
        val callbackAndText = getDelimitedCallbackFromText(trimText)

        return ParsedCallback(getCallbackFromText(callbackAndText.first), callbackAndText.second)
    }

    private fun getCallbackFromText(text: String): Callback {
        val result = Callback.entries.firstOrNull { text == it.name } ?: Callback.UNKNOWN
        if (result == Callback.UNKNOWN) log.debug("Can't parse command: $text")
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