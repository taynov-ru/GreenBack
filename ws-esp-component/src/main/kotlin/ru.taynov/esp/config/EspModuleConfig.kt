package ru.taynov.esp.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableJpaRepositories(basePackages = ["ru.taynov.esp"])
@EntityScan(basePackages = ["ru.taynov.esp.entity"])
class EspModuleConfig {

}