package io.github.chatai.util

import java.time.LocalDateTime

interface TimeProvider {
    fun now(): LocalDateTime
}

class SystemTmeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}