package io.github.chatai.chat.infrastructure

import io.github.chatai.chat.domain.Thread
import io.github.chatai.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component

interface ThreadRepository {
    fun save(thread: Thread): Thread
    fun findLatestByUser(user: User): Thread?
}

interface ThreadJpaRepository : JpaRepository<Thread, Long> {
    @Query("SELECT t FROM Thread t WHERE t.user = :user ORDER BY t.updatedAt DESC")
    fun findTopByUserOrderByUpdatedAtDesc(@Param("user") user: User): Thread?
}

@Component
class ThreadRepositoryImpl(
    private val threadJpaRepository: ThreadJpaRepository
) : ThreadRepository {
    
    override fun save(thread: Thread): Thread {
        return threadJpaRepository.save(thread)
    }
    
    override fun findLatestByUser(user: User): Thread? {
        return threadJpaRepository.findTopByUserOrderByUpdatedAtDesc(user)
    }
}