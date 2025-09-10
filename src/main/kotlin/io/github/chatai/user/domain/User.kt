package io.github.chatai.user.domain

import io.github.chatai.util.TimeProvider
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

@Table(name = "users")
@Entity
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    fun signIn(rawPassword: String, encoder: PasswordEncoder): Boolean {
        return encoder.matches(rawPassword, this.password)
    }
}

enum class Role {
    MEMBER,
    ADMIN
}
