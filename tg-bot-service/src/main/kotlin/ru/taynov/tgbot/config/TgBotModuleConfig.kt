package ru.taynov.tgbot.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories


@Configuration
@EnableJpaRepositories(basePackages = ["ru.taynov.tgbot"])
@EntityScan(basePackages = ["ru.taynov.tgbot.entity"])
class TgBotModuleConfig {

}