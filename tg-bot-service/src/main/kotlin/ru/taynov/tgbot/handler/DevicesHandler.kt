package ru.taynov.tgbot.handler

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import ru.taynov.tgbot.callback.Callback
import ru.taynov.tgbot.callback.ParsedCallback
import ru.taynov.tgbot.command.Command
import ru.taynov.tgbot.command.ParsedCommand
import ru.taynov.tgbot.dto.DeviceDto
import ru.taynov.tgbot.dto.OperateResultDto
import ru.taynov.tgbot.dto.toOperateResult
import ru.taynov.tgbot.enums.State
import ru.taynov.tgbot.service.DeviceService
import ru.taynov.tgbot.service.UserService
import ru.taynov.tgbot.validator.DeviceValidator

@Component
class DevicesHandler(
    private val deviceService: DeviceService,
    private val userService: UserService,
    private val deviceValidator: DeviceValidator,
) : MessageHandler {

    override fun operateCommand(chatId: String, parsedCommand: ParsedCommand, message: Message): OperateResultDto? {
        return when (parsedCommand.command) {
            Command.DEVICES -> getDevices(chatId)
            Command.NONE -> operateMessage(chatId, parsedCommand.payload, message)
            else -> null
        }?.toOperateResult()
    }

    override fun operateCallback(chatId: String, parsedCallback: ParsedCallback, message: Message): OperateResultDto? {
        return when (parsedCallback.callback) {
            Callback.SET_DEVICE -> setDevice(chatId, parsedCallback.payload)
            Callback.ADD_DEVICE -> addDevice(chatId)
            Callback.EDIT_NAME_DEVICE -> editDeviceName(chatId)
            Callback.DELETE_DEVICE -> deleteDevice(chatId)
            else -> null
        }?.toOperateResult()
    }

    private fun operateMessage(chatId: String, text: String, message: Message): SendMessage? {
        val user = userService.getUser(chatId)
        return when (user.state) {
            State.ADD_DEVICE_ENTER_ID -> enterDeviceId(chatId, text)
            State.ADD_DEVICE_ENTER_NAME -> enterDeviceName(chatId, text)

            else -> null
        }
    }

    private fun editDeviceName(chatId: String): SendMessage {
        userService.setState(chatId, State.ADD_DEVICE_ENTER_NAME)
        return SendMessage(chatId, "Введи новое название")
    }

    private fun deleteDevice(chatId: String): SendMessage {
        deviceService.deleteDevice(chatId)
        return SendMessage(chatId, "Устройство удалено")
    }

    private fun enterDeviceName(chatId: String, text: String): SendMessage {
        val device = deviceService.setDeviceName(chatId, text)
        userService.setState(chatId, State.NONE)
        return SendMessage(chatId, "Установлено название: ${device.name}")
    }

    private fun enterDeviceId(chatId: String, text: String): SendMessage {
        deviceValidator.validateDeviceId(text)
        deviceService.createDevice(chatId, text)
        userService.setState(chatId, State.ADD_DEVICE_ENTER_NAME)
        return SendMessage(chatId, "Устройство добавлено. Введи название")
    }

    private fun addDevice(chatId: String): SendMessage {
        userService.setState(chatId, State.ADD_DEVICE_ENTER_ID)
        return SendMessage(chatId, "Введи код устройства")
    }

    private fun setDevice(chatId: String, deviceId: String): SendMessage {
        val device = deviceService.getDeviceByChatId(chatId, deviceId)
        val text = if (device == null) {
            "Устройство не найдено"
        } else {
            deviceService.setDeviceForUser(chatId, device.id)
            "Выбрано устройство: ${device.name}"
        }
        return SendMessage(chatId, text)
    }

    private fun getDevices(chatId: String): SendMessage {
        val deviceButtons = deviceService.getDevicesByChatId(chatId)
        val keyboard: InlineKeyboardMarkup = buildInlineDeviceKeyboard(deviceButtons)
        val text = if (deviceButtons.isEmpty()) {
            "Сначала добавь устройство"
        } else {
            "Выбери устройство из списка"
        }
        return SendMessage().apply {
            this.chatId = chatId
            this.text = text
            this.replyMarkup = keyboard
        }
    }

    private fun buildInlineDeviceKeyboard(devices: List<DeviceDto>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            val buttons = mutableListOf<List<InlineKeyboardButton>>()
            keyboard = buttons
                .plus(devices.map { buildOneDeviceButton(it) })
                .plusElement(buildAddDeviceButton())
        }
    }

    private fun buildOneDeviceButton(deviceDto: DeviceDto): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = deviceDto.name
                this.callbackData = ParsedCallback(Callback.SET_DEVICE, deviceDto.id).toString()
            })
    }

    private fun buildAddDeviceButton(): List<InlineKeyboardButton> {
        return listOf(
            InlineKeyboardButton().apply {
                this.text = "Добавить ✏️"
                this.callbackData = ParsedCallback(Callback.ADD_DEVICE).toString()
            })
    }
}