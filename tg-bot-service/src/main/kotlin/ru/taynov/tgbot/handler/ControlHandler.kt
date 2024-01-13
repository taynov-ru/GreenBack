package ru.taynov.tgbot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.model.Param
import ru.taynov.tgbot.callback.Callback
import ru.taynov.tgbot.callback.ParsedCallback
import ru.taynov.tgbot.command.Command
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.InfoCardDto
import ru.taynov.tgbot.dto.OperateResultDto
import ru.taynov.tgbot.dto.toOperateResult
import ru.taynov.tgbot.enums.ModuleError
import ru.taynov.tgbot.enums.State
import ru.taynov.tgbot.service.DeviceService
import ru.taynov.tgbot.service.InteractionService
import ru.taynov.tgbot.service.UpdateParameterService
import ru.taynov.tgbot.service.UserService

@Component
class ControlHandler(
    private val userService: UserService,
    private val deviceService: DeviceService,
    private val interactionService: InteractionService,
    private val updateParameterService: UpdateParameterService,
) : MessageHandler {
    override fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto? {
        return when (parsedCommand.command) {
            Command.INFO -> getInfo(chatId)
            Command.SETTINGS -> getSettings(chatId)

            Command.NONE -> operateMessage(chatId, parsedCommand.payload, message)
            else -> null
        }
    }

    override fun operateCallback(chatId: String, parsedCallback: ParsedCallback, message: Message): OperateResultDto? {
        return when (parsedCallback.callback) {
            Callback.CHANGE_PARAMETER -> changeParameter(chatId, parsedCallback.payload, message)
            else -> null
        }
    }

    private fun operateMessage(chatId: String, text: String, message: Message): OperateResultDto? {
        val user = userService.getUser(chatId)
        return when (user.state) {
            State.CHANGE_INT_PARAMETER -> null

            else -> null
        }
    }

    private fun getSettings(chatId: String): OperateResultDto {
        val deviceId =
            userService.getUser(chatId).selectedDevice ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val data = interactionService.getLast(deviceId)

        return SendMessage().apply {
            this.chatId = chatId
            this.text = "Настройка параметров 🔧"
            this.replyMarkup = buildSettingsKeyboard(data.params)
        }.toOperateResult()
    }

    private fun getInfo(chatId: String): OperateResultDto {
        val deviceId =
            userService.getUser(chatId).selectedDevice ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val data = interactionService.getLast(deviceId)
        val deviceName =
            deviceService.getDeviceByChatId(chatId, deviceId)?.name ?: throw ModuleError.UNKNOWN_DEVICE.getException()
        return buildInfoMessage(chatId, data, deviceName, data).toOperateResult()
    }

    private fun changeParameter(chatId: String, payload: String, message: Message): OperateResultDto? {
        val parameter = runCatching { ParamName.valueOf(payload) }.getOrNull() ?: return null
        val deviceId =
            userService.getUser(chatId).selectedDevice ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        if (parameter.type == Boolean::class) {
            updateParameterService.inverseBooleanParameter(deviceId, parameter)
        }
        if (parameter.type == Int::class) {
            userService.setState(chatId, State.CHANGE_INT_PARAMETER)
            return SendMessage().apply {
                this.chatId = chatId
                this.text = "Введи целое число для параметра: ${parameter.value}"
            }.toOperateResult()
        }

        val prevMessage = if (message.text.contains("Настройка"))
            getSettings(chatId).result as SendMessage
        else
            getInfo(chatId).result as SendMessage

        return EditMessageText().apply {
            this.chatId = chatId
            this.messageId = message.messageId
            this.text = prevMessage.text
            this.replyMarkup = prevMessage.replyMarkup as InlineKeyboardMarkup
        }.toOperateResult()
    }

    private fun buildInfoMessage(
        chatId: String,
        cardDto: InfoCardDto,
        deviceName: String,
        data: InfoCardDto
    ): SendMessage {
        val text = """
            Информация об устройстве: $deviceName. 
            ${cardDto.sensorsAndParams.joinToString(separator = "\n\n", prefix = "\n") { "${it.name}: ${it.value}" }}
            """.trimIndent()

        return SendMessage().apply {
            this.chatId = chatId
            this.text = text
            this.replyMarkup = buildInfoButtons(data.params.filter { InfoCardDto.allowedParams.contains(it.name) })
        }
    }

    private fun buildSettingsKeyboard(params: List<Param>): InlineKeyboardMarkup {
        val buttons = params.map { buildChangeParameterButton(it) }
            .plusElement(buildEditDeviceButton())
            .plusElement(buildDeleteDeviceButton())
        return InlineKeyboardMarkup().apply {
            keyboard = buttons
        }
    }

    private fun buildInfoButtons(params: List<Param>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = params.map { buildChangeParameterButton(it) }
        }
    }

    private fun buildDeleteDeviceButton(): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = "Удалить устройство из списка 🗑"
                this.callbackData = ParsedCallback(Callback.DELETE_DEVICE).toString()
            })
    }

    private fun buildEditDeviceButton(): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = "Переименовать устройство  ✏️"
                this.callbackData = ParsedCallback(Callback.EDIT_NAME_DEVICE).toString()
            })
    }

    private fun buildChangeParameterButton(param: Param): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = getText(param)
                this.callbackData = ParsedCallback(Callback.CHANGE_PARAMETER, param.name.name).toString()
            })
    }

    private fun getText(param: Param): String {
        if (param.name.type == Boolean::class) {
            return (if (param.value == 0) "Включить" else "Выключить") + " " + param.name.value.lowercase()
        }
        return param.name.value
    }
}