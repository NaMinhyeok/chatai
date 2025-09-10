package io.github.chatai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChataiApplication

fun main(args: Array<String>) {
    runApplication<ChataiApplication>(*args)
}
