package io.github.chatai.chat.domain

import io.github.chatai.util.TimeProvider
import java.time.LocalDateTime

class Chat(
    val id: Long?,
    val thread: Thread,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime
) {

    companion object {
        fun create(thread: Thread, question: String, answer: String, timeProvider: TimeProvider): Chat {
            require(question.isNotBlank()) { "question must not be blank" }
            require(answer.isNotBlank()) { "answer must not be blank" }
            return Chat(
                id = null,
                thread = thread,
                question = question,
                answer = answer,
                createdAt = timeProvider.now()
            )
        }
    }
}
