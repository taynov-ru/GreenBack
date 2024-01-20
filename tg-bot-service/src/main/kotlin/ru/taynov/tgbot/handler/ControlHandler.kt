package ru.taynov.tgbot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.taynov.esp.enums.ParamName
import ru.taynov.esp.enums.WindowMode
import ru.taynov.esp.model.Param
import ru.taynov.tgbot.callback.Callback
import ru.taynov.tgbot.callback.ParsedCallback
import ru.taynov.tgbot.callback.ParsedCallback.ParameterEnum.PARENT_MESSAGE
import ru.taynov.tgbot.command.Command
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.InfoCardDto
import ru.taynov.tgbot.dto.OperateResultDto
import ru.taynov.tgbot.dto.toOperateResult
import ru.taynov.tgbot.enums.ModuleError
import ru.taynov.tgbot.service.DeviceService
import ru.taynov.tgbot.service.InteractionService
import ru.taynov.tgbot.service.UpdateParameterService
import ru.taynov.tgbot.service.UserService
import ru.taynov.tgbot.state.ExtendedState
import ru.taynov.tgbot.state.State
import java.io.Serializable

@Component
class ControlHandler(
    private val userService: UserService,
    private val deviceService: DeviceService,
    private val interactionService: InteractionService,
    private val updateParameterService: UpdateParameterService,
) : MessageHandler {
    override fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto? {
        return when (parsedCommand.command) {
            Command.INFO -> getInfo(chatId).toOperateResult()
            Command.SETTINGS -> getSettings(chatId).toOperateResult()
            else -> null
        }
    }

    override fun operateCallback(chatId: String, parsedCallback: ParsedCallback, message: Message): OperateResultDto? {
        return when (parsedCallback.callback) {
            Callback.CHANGE_PARAMETER -> changeParameter(chatId, parsedCallback.payload, message)
            Callback.TO_SETTINGS -> getSettings(chatId)
            Callback.UPDATE_INFO -> updateMessageAfterChangeParameter(chatId, message.messageId, INFO)
            Callback.WINDOW_MODE -> setWindowMode(chatId, parsedCallback, message)
            else -> null
        }?.toOperateResult()
    }

    override fun operateMessage(chatId: String, extendedState: ExtendedState, message: Message): OperateResultDto? {
        return when (extendedState.state) {
            State.CHANGE_INT_PARAMETER -> changeIntParameter(chatId, extendedState.payload, message.text)

            else -> null
        }?.toOperateResult()
    }

    private fun changeIntParameter(chatId: String, payload: String?, text: String): SendMessage {
        val deviceId =
            userService.getUser(chatId).selectedDevice ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val param = payload?.let { ParamName.valueOf(it) } ?: throw ModuleError.PARAMETER_NOT_FOUND.getException()
        val value = runCatching { text.toInt() }.getOrNull() ?: throw ModuleError.VALUE_INCORRECT.getException()
        updateParameterService.setIntParameter(deviceId, param, value)
        userService.setState(chatId, State.NONE)
        return SendMessage().apply {
            this.chatId = chatId
            this.text = "Значение установлено ✅"
            this.replyMarkup = buildToSettingsButton()
        }
    }

    private fun setWindowMode(chatId: String, payload: ParsedCallback, message: Message): EditMessageText {
        val user = userService.getUser(chatId)
        val deviceId = user.selectedDevice ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val state = payload.getParameter(PARENT_MESSAGE) ?: INFO
        val mode = WindowMode.valueOf(payload.payload)
        updateParameterService.setWindowModeParameter(deviceId, mode)

        return updateMessageAfterChangeParameter(chatId, message.messageId, state)
    }

    private fun getSettings(chatId: String): SendMessage {
        val deviceId = userService.getUser(chatId).selectedDevice
            ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val data = interactionService.getLast(deviceId)

        return SendMessage().apply {
            this.chatId = chatId
            this.text = "Настройка параметров 🔧"
            this.replyMarkup = buildSettingsKeyboard(data.params)
        }
    }

    private fun getInfo(chatId: String): SendMessage {
        val deviceId =
            userService.getUser(chatId).selectedDevice ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val data = interactionService.getLast(deviceId)
        val deviceName = deviceService.getDeviceByChatId(chatId, deviceId)?.name
            ?: throw ModuleError.UNKNOWN_DEVICE.getException()
        return buildInfoMessage(chatId, data, deviceName, data)
    }

    private fun changeParameter(
        chatId: String,
        payload: String,
        message: Message
    ): BotApiMethod<out Serializable>? {
        val parameter = runCatching { ParamName.valueOf(payload) }.getOrNull() ?: return null
        val deviceId = userService.getUser(chatId).selectedDevice
            ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()

        if (parameter.type == Int::class) {
            return setStateChangeIntParameter(chatId, parameter)
        }

        if (parameter.type == Boolean::class) {
            updateParameterService.inverseBooleanParameter(deviceId, parameter)
        }

        val fromMessage = if (message.text.contains("Настройка")) SETTINGS else INFO

        if (parameter.type == WindowMode::class) {
            return buildSetWindowModeKeyboard(chatId, message.messageId, fromMessage)
        }

        return updateMessageAfterChangeParameter(chatId, message.messageId, fromMessage)
    }

    private fun updateMessageAfterChangeParameter(
        chatId: String,
        messageId: Int,
        fromMessage: String
    ): EditMessageText {
        val prevMessage = if (fromMessage == SETTINGS)
            getSettings(chatId)
        else
            getInfo(chatId)

        return EditMessageText().apply {
            this.chatId = chatId
            this.messageId = messageId
            this.text = prevMessage.text
            this.replyMarkup = prevMessage.replyMarkup as InlineKeyboardMarkup
        }
    }

    private fun setStateChangeIntParameter(chatId: String, parameter: ParamName): SendMessage {
        val deviceId = userService.getUser(chatId).selectedDevice
            ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val parameterValue = interactionService.getParameterValue(deviceId, parameter)

        userService.setState(chatId, ExtendedState(State.CHANGE_INT_PARAMETER, parameter.name))
        return SendMessage().apply {
            this.chatId = chatId
            this.text = """
                Текущее значение параметра: ${parameter.value}
                $parameterValue
                
                Введи целое число
                """.trimIndent()
        }
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
            keyboard = listOf(buildUpdateInfoButton())
                .plus(params.map { buildChangeParameterButton(it) })
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

    private fun buildUpdateInfoButton(): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = "Обновить информацию 🔄"
                this.callbackData = ParsedCallback(Callback.UPDATE_INFO).toString()
            })
    }

    private fun buildChangeParameterButton(param: Param): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = getText(param)
                this.callbackData = ParsedCallback(Callback.CHANGE_PARAMETER, param.name.name).toString()
            })
    }

    private fun buildToSettingsButton(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(listOf(InlineKeyboardButton().apply {
                this.text = "К настройкам"
                this.callbackData = ParsedCallback(Callback.TO_SETTINGS).toString()
            }))
        }
    }

    private fun buildSetWindowModeKeyboard(chatId: String, messageId: Int, fromMessage: String): EditMessageText {
        return EditMessageText().apply {
            this.chatId = chatId
            this.messageId = messageId
            this.text = "Режим форточки"
            this.replyMarkup = InlineKeyboardMarkup().apply {
                keyboard = listOf(WindowMode.entries.map {
                    InlineKeyboardButton().apply {
                        this.text = it.value
                        this.callbackData = ParsedCallback(Callback.WINDOW_MODE, it.name).apply {
                            setParameter(
                                PARENT_MESSAGE,
                                fromMessage
                            )
                        }.toString()
                    }
                })
            }
        }
    }

    private fun getText(param: Param): String {
        if (param.name.type == Boolean::class) {
            return (if (param.value == 0) "Включить" else "Выключить") + " " + param.name.value.lowercase()
        }
        if (param.name.type == Int::class) {
            return param.name.value + " " + param.value
        }
        if (param.name.type == WindowMode::class) {
            return param.name.value + ": " + WindowMode.valueFromOrdinal(param.value).value
        }
        return param.name.value
    }

    companion object {
        private const val INFO: String = "info"
        private const val SETTINGS: String = "settings"
    }
}