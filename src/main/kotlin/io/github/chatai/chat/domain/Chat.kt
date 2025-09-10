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
    
    /**
     * 이 대화를 OpenAI 메시지 형식으로 변환합니다.
     * 사용자 질문과 어시스턴트 답변을 순서대로 반환합니다.
     */
    fun toOpenAIMessages(): List<io.github.chatai.chat.application.OpenAIMessage> {
        return listOf(
            io.github.chatai.chat.application.OpenAIMessage(
                role = io.github.chatai.chat.application.MessageRole.USER,
                content = question
            ),
            io.github.chatai.chat.application.OpenAIMessage(
                role = io.github.chatai.chat.application.MessageRole.ASSISTANT,
                content = answer
            )
        )
    }
}
