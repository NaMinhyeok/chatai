package io.github.chatai.chat.domain

import io.github.chatai.user.domain.User
import io.github.chatai.util.TimeProvider
import jakarta.persistence.*
import java.time.Duration
import java.time.LocalDateTime

@Table(name = "threads")
@Entity
class Thread(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime,
    
    @OneToMany(mappedBy = "thread", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
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
