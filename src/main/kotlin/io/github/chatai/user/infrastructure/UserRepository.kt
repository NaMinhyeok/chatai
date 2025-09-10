package io.github.chatai.user.infrastructure

import io.github.chatai.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
}

interface UserJpaRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}

@Component
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun save(user: User): User {
        return userJpaRepository.save(user)
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
    }
}