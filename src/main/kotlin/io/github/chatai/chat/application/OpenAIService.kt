package io.github.chatai.chat.application

/**
 * OpenAI API 호출을 담당하는 서비스 인터페이스
 */
interface OpenAIService {
    fun generateResponse(messages: List<OpenAIMessage>): String
}

/**
 * OpenAI API 메시지 형식
 */
data class OpenAIMessage(
    val role: MessageRole,
    val content: String
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}