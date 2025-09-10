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
    private val timeProvider: TimeProvider
) {

    fun sendMessage(userEmail: String, question: String, answer: String): ChatResponse {
        val user = userRepository.findByEmail(userEmail) 
            ?: throw IllegalArgumentException("존재하지 않는 사용자입니다.")
            
        val thread = findOrCreateThread(user)
        val chat = Chat.create(thread, question, answer, timeProvider)
        
        val updatedThread = thread.touch(timeProvider)
        threadRepository.save(updatedThread)
        
        val savedChat = chatRepository.save(chat)
        
        return ChatResponse(
            threadId = updatedThread.id!!,
            question = savedChat.question,
            answer = savedChat.answer,
            createdAt = savedChat.createdAt
        )
    }

    private fun findOrCreateThread(user: User): Thread {
        val latestThread = threadRepository.findLatestByUser(user)
        
        return if (latestThread == null || latestThread.isExpired(timeProvider.now())) {
            val newThread = Thread.start(user, timeProvider)
            threadRepository.save(newThread)
        } else {
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