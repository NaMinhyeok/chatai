package io.github.chatai.util

import java.time.LocalDateTime
import org.springframework.stereotype.Component

interface TimeProvider {
    fun now(): LocalDateTime
}

@Component
class SystemTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}
