package ru.taynov.tgbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan


@ComponentScan(basePackages = ["ru.taynov.esp", "ru.taynov.tgbot"])
@SpringBootApplication
@EntityScan(basePackages = ["ru.taynov.esp.entity"])
@ConfigurationPropertiesScan
@EnableConfigurationProperties
class GreenBackApplication

fun main(args: Array<String>) {

    runApplication<GreenBackApplication>(*args)
}
