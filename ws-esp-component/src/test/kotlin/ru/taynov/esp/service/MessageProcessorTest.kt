package ru.taynov.esp.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import ru.taynov.esp.model.EspMessage
import ru.taynov.esp.repository.EspDataRepository

class MessageProcessorTest {

    private val espInteractionService: EspInteractionService = mock(EspInteractionService::class.java)
    private val dataRepository: EspDataRepository = mock(EspDataRepository::class.java)
    private val message = EspMessage(id = "123", type = "type", sensors = null, params = null)

    private val messageProcessor: MessageProcessor = MessageProcessor(espInteractionService, dataRepository)


    @Test
    fun shouldIgnoreExceptionInCheckAlarm() {
        `when`(espInteractionService.getLast(anyString())).thenThrow(RuntimeException::class.java)

        messageProcessor.process(message)

        verify(dataRepository).save(any())
    }
}