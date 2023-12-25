package ru.taynov.esp.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.taynov.esp.entity.EspDataEntity

@Repository
interface EspDataRepository : CrudRepository<EspDataEntity, String> {

    fun findDistinctById(id: String): EspDataEntity?
}