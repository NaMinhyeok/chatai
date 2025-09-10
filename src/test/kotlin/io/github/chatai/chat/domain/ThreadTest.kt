package io.github.chatai.chat.domain

import io.github.chatai.user.domain.Role
import io.github.chatai.user.domain.User
import io.github.chatai.util.TimeProvider
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class ThreadTest {

    class FakeTimeProvider(private var current: LocalDateTime) : TimeProvider {
        override fun now(): LocalDateTime = current
        fun advanceTo(time: LocalDateTime) {
            current = time
        }
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
    fun `메시지를 추가하면 스레드에 저장되고 업데이트 시간이 갱신된다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        // when
        val afterOneMinute = createdAt.plusMinutes(1)
        clock.advanceTo(afterOneMinute)
        val updated = thread.addMessage(
            question = "프로젝트 구조를 간단히 설명해줘",
            answer = "레이어는 api, application, domain으로 구성돼요.",
            timeProvider = clock
        )
        // then
        then(updated.messages).hasSize(1)
        then(updated.messages[0].question).isEqualTo("프로젝트 구조를 간단히 설명해줘")
        then(updated.updatedAt).isEqualTo(afterOneMinute)
    }

    @Test
    fun `스레드를 시작하면 생성 및 수정 시간이 동일하다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        // when
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        // then
        then(thread.createdAt).isEqualTo(createdAt)
        then(thread.updatedAt).isEqualTo(createdAt)
        then(thread.user.name).isEqualTo("민혁")
    }

    @Test
    fun `30분 미만이면 만료되지 않는다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        // when
        val after29Minutes = createdAt.plusMinutes(29)
        // then
        then(thread.isExpired(after29Minutes)).isFalse()
    }

    @Test
    fun `정확히 30분이 지나면 만료된다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        // when
        val after30Minutes = createdAt.plusMinutes(30)
        // then
        then(thread.isExpired(after30Minutes)).isTrue()
    }

    @Test
    fun `터치하면 updatedAt이 현재 시간으로 변경된 새 인스턴스를 반환한다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val thread = Thread.start(user = createMember(clock), timeProvider = clock)
        // when
        val after5Minutes = createdAt.plusMinutes(5)
        clock.advanceTo(after5Minutes)
        val touched = thread.touch(clock)
        // then
        then(touched.updatedAt).isEqualTo(after5Minutes)
        then(touched.createdAt).isEqualTo(thread.createdAt)
        then(touched).isNotSameAs(thread)
    }
}
