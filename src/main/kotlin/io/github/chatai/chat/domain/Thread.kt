package io.github.chatai.chat.domain

import io.github.chatai.user.domain.User
import io.github.chatai.util.TimeProvider
import java.time.Duration
import java.time.LocalDateTime

class Thread(
    val id: Long?,
    val user: User,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val messages: List<Chat> = emptyList()
) {

    companion object {
        fun start(user: User, timeProvider: TimeProvider): Thread {
            val now = timeProvider.now()
            return Thread(
                id = null,
                user = user,
                createdAt = now,
                updatedAt = now,
                messages = emptyList()
            )
        }
    }

    fun isExpired(now: LocalDateTime, timeoutMinutes: Long = 30): Boolean {
        val elapsed = Duration.between(this.updatedAt, now)
        return elapsed.toMinutes() >= timeoutMinutes
    }

    fun touch(timeProvider: TimeProvider): Thread {
        return Thread(
            id = this.id,
            user = this.user,
            createdAt = this.createdAt,
            updatedAt = timeProvider.now(),
            messages = this.messages
        )
    }

    fun addMessage(question: String, answer: String, timeProvider: TimeProvider): Thread {
        val chat = Chat.create(this, question, answer, timeProvider)
        val updatedMessages = this.messages + chat
        return Thread(
            id = this.id,
            user = this.user,
            createdAt = this.createdAt,
            updatedAt = timeProvider.now(),
            messages = updatedMessages
        )
    }
}
