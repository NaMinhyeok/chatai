package io.github.chatai.chat.api

import io.github.chatai.chat.application.ChatService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping("/message")
    fun sendMessage(
        @RequestBody request: ChatMessageRequest
    ): ResponseEntity<ChatMessageResponse> {
        val response = chatService.sendMessage(
            userEmail = request.userEmail,
            question = request.question
        )
        
        return ResponseEntity.ok(
            ChatMessageResponse(
                threadId = response.threadId,
                question = response.question,
                answer = response.answer,
                createdAt = response.createdAt
            )
        )
    }
}

data class ChatMessageRequest(
    val userEmail: String,  // TODO: JWT에서 추출하도록 변경 예정
    val question: String
)

data class ChatMessageResponse(
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime
)