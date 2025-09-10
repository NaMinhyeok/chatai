package io.github.chatai.chat.domain

import io.github.chatai.util.TimeProvider
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "chats")
@Entity
class Chat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    val thread: Thread,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val answer: String,
    
    @Column(name = "created_at")
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
