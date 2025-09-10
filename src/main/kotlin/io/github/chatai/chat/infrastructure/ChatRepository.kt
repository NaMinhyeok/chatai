package io.github.chatai.chat.infrastructure

import io.github.chatai.chat.domain.Chat
import io.github.chatai.chat.domain.Thread
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component

interface ChatRepository {
    fun save(chat: Chat): Chat
    fun findByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat>
}

interface ChatJpaRepository : JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c WHERE c.thread = :thread ORDER BY c.createdAt ASC")
    fun findByThreadOrderByCreatedAtAsc(@Param("thread") thread: Thread): List<Chat>
}

@Component
class ChatRepositoryImpl(
    private val chatJpaRepository: ChatJpaRepository
) : ChatRepository {
    
    override fun save(chat: Chat): Chat {
        return chatJpaRepository.save(chat)
    }
    
    override fun findByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat> {
        return chatJpaRepository.findByThreadOrderByCreatedAtAsc(thread)
    }
}