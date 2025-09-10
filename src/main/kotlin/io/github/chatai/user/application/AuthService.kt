package io.github.chatai.user.application

import io.github.chatai.user.domain.User
import io.github.chatai.user.infrastructure.UserRepository
import io.github.chatai.util.TimeProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val encoder: PasswordEncoder,
    private val timeProvider: TimeProvider,
    private val userRepository: UserRepository
) {
    fun signUp(
        email: String,
        password: String,
        name: String,
    ) {
        User.signUp(
            email = email,
            password = password,
            name = name,
            encoder = encoder,
            timeProvider = timeProvider
        ).let {
            userRepository.save(it)
        }
    }

    fun signIn(
        email: String,
        password: String
    ): Boolean {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("존재하지 않는 이메일입니다.")
        return user.signIn(password, encoder)
    }
}
