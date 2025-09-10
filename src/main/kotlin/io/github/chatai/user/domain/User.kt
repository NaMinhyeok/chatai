package io.github.chatai.user.domain

import io.github.chatai.util.TimeProvider
import org.springframework.data.auditing.DateTimeProvider
import java.time.LocalDateTime

class User(
    val id: Long?,
    val email: String,
    val password: String,
    val name: String,
    val role: Role,
    val regTs: LocalDateTime
) {

    companion object {
        fun signUp(
            email: String,
            password: String,
            name: String,
            role: Role = Role.MEMBER,
            encoder: PasswordEncoder,
            timeProvider: TimeProvider
        ): User {
            return User(
                email = email,
                password = encoder.encode(password),
                name = name,
                role = role,
                id = null,
                regTs = timeProvider.now()
            )
        }
    }
}

enum class Role {
    MEMBER,
    ADMIN
}

interface PasswordEncoder {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}