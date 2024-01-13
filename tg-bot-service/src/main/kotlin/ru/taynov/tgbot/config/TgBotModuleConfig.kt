package ru.taynov.tgbot.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor


@Configuration
@EnableJpaRepositories(basePackages = ["ru.taynov.tgbot"])
@EntityScan(basePackages = ["ru.taynov.tgbot.entity"])
class TgBotModuleConfig {

}