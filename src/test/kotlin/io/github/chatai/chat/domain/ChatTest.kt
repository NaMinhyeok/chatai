package io.github.chatai.chat.domain

import io.github.chatai.user.domain.Role
import io.github.chatai.user.domain.User
import io.github.chatai.util.TimeProvider
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class ChatTest {

    class FakeTimeProvider(private val fixed: LocalDateTime) : TimeProvider {
        override fun now(): LocalDateTime = fixed
    }

    class StubPasswordEncoder : PasswordEncoder {
        override fun encode(rawPassword: CharSequence?): String {
            return "encoded-$rawPassword"
        }

        override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean {
            return encodedPassword == encode(rawPassword)
        }
    }

    private fun createMember(tp: TimeProvider): User = User.signUp(
        email = "minhyeok@gmail.com",
        password = "secure-password",
        name = "민혁",
        role = Role.MEMBER,
        encoder = StubPasswordEncoder(),
        timeProvider = tp
    )

    @Test
    fun `대화 생성 시 질문과 답변이 저장되고 생성 시간이 설정된다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        // when
        val chat = Chat.create(
            thread = thread,
            question = "요구사항에 맞는 도메인 설계를 설명해줘",
            answer = "Thread와 Chat은 같은 집합으로 객체 참조를 사용합니다.",
            timeProvider = clock
        )
        // then
        then(chat.createdAt).isEqualTo(createdAt)
        then(chat.question).isEqualTo("요구사항에 맞는 도메인 설계를 설명해줘")
        then(chat.answer).isEqualTo("Thread와 Chat은 같은 집합으로 객체 참조를 사용합니다.")
        then(chat.thread).isEqualTo(thread)
    }

    @Test
    fun `질문이 비어있으면 예외가 발생한다`() {
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        assertThrows(IllegalArgumentException::class.java) {
            Chat.create(thread = thread, question = "  \t\n", answer = "A", timeProvider = clock)
        }
    }

    @Test
    fun `답변이 비어있으면 예외가 발생한다`() {
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        assertThrows(IllegalArgumentException::class.java) {
            Chat.create(thread = thread, question = "Q", answer = "  ", timeProvider = clock)
        }
    }
}
