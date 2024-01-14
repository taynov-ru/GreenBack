package ru.taynov.tgbot.handler

import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.taynov.tgbot.callback.Callback
import ru.taynov.tgbot.command.Command
import ru.taynov.tgbot.state.ExtendedState
import ru.taynov.tgbot.state.State

@Component
class HandlerProvider(
    private val defaultHandler: DefaultHandler,
    private val devicesHandler: DevicesHandler,
    private val systemHandler: SystemHandler,
    private val controlHandler: ControlHandler,
) {
    private val log = KotlinLogging.logger {}

    fun handlerForCommand(command: Command): MessageHandler {
        return when (command) {
            Command.NONE -> {
                log.info("Handler for message. Return DefaultHandler")
                defaultHandler
            }

            Command.START, Command.HELP -> {
                log.info("Handler for command[$command] is: $systemHandler")
                systemHandler
            }

            Command.DEVICES -> {
                log.info("Handler for command[$command] is: $devicesHandler")
                devicesHandler
            }

            Command.INFO, Command.SETTINGS -> {
                log.info("Handler for command[$command] is: $controlHandler")
                controlHandler
            }

            else -> {
                log.info("Handler for command[$command] not Set. Return DefaultHandler")
                defaultHandler
            }
        }
    }

    fun handlerForCallback(callback: Callback): MessageHandler {
        return when (callback) {
            Callback.UNKNOWN -> {
                log.warn("Null command accepted. This is not good scenario.")
                defaultHandler
            }

            Callback.SET_DEVICE, Callback.ADD_DEVICE, Callback.DELETE_DEVICE, Callback.EDIT_NAME_DEVICE -> {
                log.info("Handler for callback[$callback] is $devicesHandler")
                devicesHandler
            }

            Callback.CHANGE_PARAMETER, Callback.TO_SETTINGS, Callback.UPDATE_INFO, Callback.WINDOW_MODE -> {
                log.info("Handler for callback[$callback] is $controlHandler")
                controlHandler
            }

            else -> {
                log.info("Handler for callback[$callback] not Set. Return DefaultHandler")
                defaultHandler
            }
        }
    }

    fun handlerByState(extendedState: ExtendedState): MessageHandler {
        return when (extendedState.state) {
            State.NONE -> defaultHandler
            State.CHANGE_INT_PARAMETER -> controlHandler
            State.ADD_DEVICE_ENTER_ID, State.ADD_DEVICE_ENTER_NAME -> devicesHandler
        }
    }
}
