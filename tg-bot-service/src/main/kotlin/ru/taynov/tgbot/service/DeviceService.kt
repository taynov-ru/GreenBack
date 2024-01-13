package ru.taynov.tgbot.service

import org.springframework.stereotype.Service
import ru.taynov.tgbot.dto.DeviceDto
import ru.taynov.tgbot.dto.toDeviceDto
import ru.taynov.tgbot.entity.DeviceUserAssignmentEntity
import ru.taynov.tgbot.entity.DeviceInfoEntity
import ru.taynov.tgbot.entity.UserStateEntity
import ru.taynov.tgbot.enums.ModuleError
import ru.taynov.tgbot.repository.DeviceAssignmentRepository
import ru.taynov.tgbot.repository.DeviceRepository
import ru.taynov.tgbot.repository.UserStateRepository
import kotlin.jvm.optionals.getOrNull

@Service
class DeviceService(
    private val repository: DeviceRepository,
    private val userStateRepository: UserStateRepository,
    private val deviceAssignmentRepository: DeviceAssignmentRepository,
) {

    fun getDevicesByChatId(chatId: String): List<DeviceDto> {
        return repository.findAllByUserId(chatId.toLong()).map { it.toDeviceDto() }
    }

    fun getDeviceByChatId(chatId: String, deviceId: String): DeviceDto? {
        return repository.findByUserIdAndDeviceId(chatId.toLong(), deviceId)?.toDeviceDto()
    }

    fun setDeviceForUser(chatId: String, deviceId: String) {
        val user = userStateRepository.findById(chatId.toLong())
            .orElse(UserStateEntity(userId = chatId.toLong()))
        user.selectedDevice = deviceId
        userStateRepository.save(user)
    }

    fun createDevice(chatId: String, deviceId: String) {
        val device = DeviceInfoEntity(userId = chatId.toLong(), deviceId = deviceId)
        repository.save(device)
        saveDeviceAssignment(chatId, deviceId)
        userStateRepository.findById(chatId.toLong()).get().also {
            it.selectedDevice = deviceId
            userStateRepository.save(it)
        }
    }

    fun setDeviceName(chatId: String, deviceName: String): DeviceDto {
        val deviceId = userStateRepository.findById(chatId.toLong()).get().selectedDevice
            ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        val device = repository.findByUserIdAndDeviceId(chatId.toLong(), deviceId)
            .apply { this?.name = deviceName } ?: throw ModuleError.UNKNOWN_DEVICE.getException()
        repository.save(device)
        return device.toDeviceDto()
    }

    fun deleteDevice(chatId: String) {
        val deviceId = userStateRepository.findById(chatId.toLong()).get().selectedDevice
            ?: throw ModuleError.BEFORE_SELECT_DEVICE.getException()
        repository.findByUserIdAndDeviceId(chatId.toLong(), deviceId)?.also {
            repository.delete(it)
        }
        deleteDeviceAssignment(chatId, deviceId)
        userStateRepository.findById(chatId.toLong()).ifPresent {
            it.selectedDevice = null
            userStateRepository.save(it)
        }
    }

    private fun saveDeviceAssignment(chatId: String, deviceId: String) {
        val deviceAssignment = deviceAssignmentRepository.findById(deviceId).getOrNull()
            ?: DeviceUserAssignmentEntity().apply { this.deviceId = deviceId }
        deviceAssignment.users.add(chatId)
        deviceAssignmentRepository.save(deviceAssignment)
    }

    private fun deleteDeviceAssignment(chatId: String, deviceId: String) {
        deviceAssignmentRepository.findById(deviceId).getOrNull()?.let {
            it.users.remove(chatId)
            deviceAssignmentRepository.save(it)
        }
    }
}