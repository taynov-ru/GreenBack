package ru.taynov.tgbot.command

import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.taynov.tgbot.config.BotConfig
import java.util.*

@Component
class CommandParser(private val botConfig: BotConfig) {

    private val log: KLogger = KotlinLogging.logger {}

    fun getParsedCommand(text: String?): ParsedCommand {
        var trimText = ""
        if (text != null) trimText = text.trim { it <= ' ' }
        val result = ParsedCommand(Command.NONE, trimText)
        if ("" == trimText) return result
        val commandAndText = getDelimitedCommandFromText(trimText)
        if (isCommand(commandAndText.first)) {
            return if (isCommandForMe(commandAndText.first)) {
                val commandForParse = cutCommandFromFullText(commandAndText.first)
                val commandFromText: Command = getCommandFromText(commandForParse)
                ParsedCommand(commandFromText, commandAndText.second)
            } else {
                ParsedCommand(Command.NOT_FOR_ME, commandAndText.second)
            }
        }
        return result
    }

    private fun cutCommandFromFullText(text: String): String {
        return if (text.contains(DELIMITER_COMMAND_BOTNAME))
            text.substring(
                1, text.indexOf(DELIMITER_COMMAND_BOTNAME)
            ) else text.substring(1)
    }

    private fun getCommandFromText(text: String): Command {
        val upperCaseText = text.uppercase(Locale.getDefault()).trim { it <= ' ' }
        var command: Command = Command.NONE
        try {
            command = Command.valueOf(upperCaseText)
        } catch (e: IllegalArgumentException) {
            command = Command.UNKNOWN
            log.debug("Can't parse command: $text")
        }
        return command
    }

    private fun getDelimitedCommandFromText(trimText: String): Pair<String, String> {
        val commandText = if (trimText.contains(" ")) {
            val indexOfSpace = trimText.indexOf(" ")
            Pair(trimText.substring(0, indexOfSpace), trimText.substring(indexOfSpace + 1))
        } else Pair(trimText, "")
        return commandText
    }

    private fun isCommandForMe(command: String): Boolean {
        if (command.contains(DELIMITER_COMMAND_BOTNAME)) {
            return botConfig.name == command.substring(command.indexOf(DELIMITER_COMMAND_BOTNAME) + 1)
        }
        return true
    }

    private fun isCommand(text: String): Boolean {
        return text.startsWith(PREFIX_FOR_COMMAND)
    }

    companion object {
        private const val PREFIX_FOR_COMMAND = "/"
        private const val DELIMITER_COMMAND_BOTNAME = "@"
    }
}