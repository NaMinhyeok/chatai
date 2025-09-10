package io.github.chatai.user.api

import io.github.chatai.user.application.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/sign-up")
    fun signUp(
        @RequestBody request: SignUpRequest
    ) {
        authService.signUp(request.email, request.password, request.name)
    }

    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody request: SignInRequest
    ) {
        authService.signIn(request.email, request.password)
        TODO("토큰을 발급해서 반환하도록 한다.")
    }
}

data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String
)

data class SignInRequest(
    val email: String,
    val password: String
)