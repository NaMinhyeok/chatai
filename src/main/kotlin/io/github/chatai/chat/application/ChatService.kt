package io.github.chatai.chat.application

import io.github.chatai.chat.domain.Chat
import io.github.chatai.chat.domain.Thread
import io.github.chatai.chat.infrastructure.ChatRepository
import io.github.chatai.chat.infrastructure.ThreadRepository
import io.github.chatai.user.domain.User
import io.github.chatai.user.infrastructure.UserRepository
import io.github.chatai.util.TimeProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ChatService(
    private val userRepository: UserRepository,
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val openAIService: OpenAIService,
    private val timeProvider: TimeProvider
) {

    fun sendMessage(userEmail: String, question: String): ChatResponse {
        val user = userRepository.findByEmail(userEmail) 
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")
            
        val thread = findOrCreateThread(user)
        
        val existingChats = chatRepository.findByThreadOrderByCreatedAtAsc(thread)
        
        val openAIMessages = buildOpenAIMessages(existingChats, question)
        
        val aiAnswer = openAIService.generateResponse(openAIMessages)
        
        val chat = Chat.create(thread, question, aiAnswer, timeProvider)
        val savedChat = chatRepository.save(chat)
        
        val updatedThread = thread.touch(timeProvider)
        threadRepository.save(updatedThread)
        
        return ChatResponse(
            threadId = updatedThread.id!!,
            question = savedChat.question,
            answer = savedChat.answer,
            createdAt = savedChat.createdAt
        )
    }


    private fun buildOpenAIMessages(existingChats: List<Chat>, newQuestion: String): List<OpenAIMessage> {
        val messages = mutableListOf<OpenAIMessage>()
        
        // 시스템 메시지 추가 (선택적)
        messages.add(OpenAIMessage(
            role = MessageRole.SYSTEM,
            content = "당신은 도움이 되고 친절한 AI 어시스턴트입니다."
        ))
        
        // 기존 대화 히스토리 추가
        existingChats.forEach { chat ->
            messages.addAll(chat.toOpenAIMessages())
        }
        
        // 새로운 사용자 질문 추가
        messages.add(OpenAIMessage(
            role = MessageRole.USER,
            content = newQuestion
        ))
        
        return messages
    }


    /**
     * 사용자의 최신 스레드를 찾거나 새로 생성합니다.
     * 30분 이내의 스레드가 있으면 재사용, 없으면 새로 생성
     */
    private fun findOrCreateThread(user: User): Thread {
        val latestThread = threadRepository.findLatestByUser(user)
        
        return if (latestThread == null || latestThread.isExpired(timeProvider.now())) {
            // 새 스레드 생성
            val newThread = Thread.start(user, timeProvider)
            threadRepository.save(newThread)
        } else {
            // 기존 스레드 사용
            latestThread
        }
    }
}

data class ChatResponse(
    val threadId: Long,
    val question: String,
    val answer: String,
    val createdAt: LocalDateTime
)