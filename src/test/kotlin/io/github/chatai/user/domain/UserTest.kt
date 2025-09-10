package io.github.chatai.user.domain

import io.github.chatai.util.TimeProvider
import org.assertj.core.api.BDDAssertions.*
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class UserTest {

    class StubPasswordEncoder : PasswordEncoder {
        override fun encode(rawPassword: CharSequence?): String? {
            return "encoded-$rawPassword"
        }

        override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
            return encodedPassword == encode(rawPassword)
        }
    }

    class TestTimeProvider : TimeProvider {
        override fun now() = LocalDateTime.of(2025, 9, 10, 0, 0)
    }

    @Test
    fun `회원가입을 하면 유저가 생성된다`() {
        // given
        // when
        val user = User.signUp(
            email = "minhyeok@gmail.com",
            password = "password",
            name = "민혁",
            encoder = StubPasswordEncoder(),
            timeProvider = TestTimeProvider()
        )
        // then
        then(user).isNotNull()
    }

    @Test
    fun `회원가입을 진행하면 권한은 멤버이다`() {
        // given
        // when
        val user = User.signUp(
            email = "minhyeok@gmail.com",
            password = "password",
            name = "민혁",
            encoder = StubPasswordEncoder(),
            timeProvider = TestTimeProvider()
        )
        // then
        then(user.role).isEqualTo(Role.MEMBER)
    }

    @Test
    fun `회원가입을 할 때 비밀번호는 평문으로 저장되어선 안된다`() {
        // given
        // when
        val userInputPassword = "password"
        val user = User.signUp(
            email = "minhyeok@gmail.com",
            password = userInputPassword,
            name = "민혁",
            encoder = StubPasswordEncoder(),
            timeProvider = TestTimeProvider()
        )
        // then
        then(user.password).isNotEqualTo(userInputPassword)
    }

    @Test
    fun `입력받은 패스워드의 해독 결과가 일치하면 로그인 성공한다`() {
        // given
        val rawPassword = "password"
        val user = User.signUp(
            email = "minhyeok@gmail.com",
            password = rawPassword,
            name = "민혁",
            encoder = StubPasswordEncoder(),
            timeProvider = TestTimeProvider()
        )
        // when
        val loginResult = user.signIn(rawPassword, StubPasswordEncoder())
        // then
        then(loginResult).isTrue()
    }

    @Test
    fun `등록된 비밀번호와 다른 비밀번호를 입력하면 로그인에 실패한다`() {
        // given
        val rawPassword = "password"
        val user = User.signUp(
            email = "minhyeok@gmail.com",
            password = rawPassword,
            name = "민혁",
            encoder = StubPasswordEncoder(),
            timeProvider = TestTimeProvider()
        )
        // when
        val loginResult = user.signIn("wrong-password", StubPasswordEncoder())
        // then
        then(loginResult).isFalse()
    }
}