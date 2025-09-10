package io.github.chatai.chat.domain

import io.github.chatai.user.domain.Role
import io.github.chatai.user.domain.User
import io.github.chatai.util.TimeProvider
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class ThreadResolverTest {

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
    fun `이전 스레드가 없으면 새 스레드를 만든다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val resolver = ThreadResolver(clock)
        val member = createMember(clock)
        // when
        val resolved = resolver.resolve(user = member, lastThread = null)
        // then
        then(resolved.user).isEqualTo(member)
        then(resolved.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `30분 이내면 기존 스레드를 유지한다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val member = createMember(clock)
        val existing = Thread.start(user = member, timeProvider = clock)
        val resolver = ThreadResolver(clock)
        // when
        clock.advanceTo(createdAt.plusMinutes(29))
        val resolved = resolver.resolve(user = member, lastThread = existing)
        // then
        then(resolved).isSameAs(existing)
    }

    @Test
    fun `정확히 30분이 지나면 새 스레드를 만든다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val member = createMember(clock)
        val existing = Thread.start(user = member, timeProvider = clock)
        val resolver = ThreadResolver(clock)
        // when
        clock.advanceTo(createdAt.plusMinutes(30))
        val resolved = resolver.resolve(user = member, lastThread = existing)
        // then
        then(resolved).isNotSameAs(existing)
        then(resolved.createdAt).isEqualTo(clock.now())
    }

    @Test
    fun `30분 초과면 새 스레드를 만든다`() {
        // given
        val createdAt = LocalDateTime.of(2025, 9, 10, 0, 0)
        val clock = FakeTimeProvider(createdAt)
        val member = createMember(clock)
        val existing = Thread.start(user = member, timeProvider = clock)
        val resolver = ThreadResolver(clock)
        // when
        clock.advanceTo(createdAt.plusMinutes(31))
        val resolved = resolver.resolve(user = member, lastThread = existing)
        // then
        then(resolved).isNotSameAs(existing)
    }
}
