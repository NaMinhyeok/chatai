package io.github.chatai.chat.application

import io.github.chatai.chat.domain.Chat
import io.github.chatai.chat.domain.Thread
import io.github.chatai.chat.infrastructure.ChatRepository
import io.github.chatai.chat.infrastructure.ThreadRepository
import io.github.chatai.user.domain.Role
import io.github.chatai.user.domain.User
import io.github.chatai.user.infrastructure.UserRepository
import io.github.chatai.util.TimeProvider
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class ChatServiceTest {

    // 테스트용 간단한 Mock 구현
    class TestUserRepository : UserRepository {
        private val users = mutableMapOf<String, User>()
        
        fun addUser(user: User) {
            users[user.email] = user
        }
        
        override fun save(user: User): User = user
        override fun findByEmail(email: String): User? = users[email]
    }
    
    class TestThreadRepository : ThreadRepository {
        private val threads = mutableMapOf<User, Thread>()
        private var nextId = 1L
        
        override fun save(thread: Thread): Thread {
            val savedThread = if (thread.id == null) {
                // 새 스레드 저장
                Thread(nextId++, thread.user, thread.createdAt, thread.updatedAt, thread.messages)
            } else {
                // 기존 스레드 업데이트
                Thread(thread.id, thread.user, thread.createdAt, thread.updatedAt, thread.messages)
            }
            threads[thread.user] = savedThread
            return savedThread
        }
        
        override fun findLatestByUser(user: User): Thread? = threads[user]
    }
    
    class TestChatRepository : ChatRepository {
        private val chats = mutableListOf<Chat>()
        private var nextId = 1L
        
        override fun save(chat: Chat): Chat {
            val savedChat = Chat(nextId++, chat.thread, chat.question, chat.answer, chat.createdAt)
            chats.add(savedChat)
            return savedChat
        }
        
        override fun findByThreadOrderByCreatedAtAsc(thread: Thread): List<Chat> {
            return chats.filter { it.thread.id == thread.id }.sortedBy { it.createdAt }
        }
    }
    
    class TestTimeProvider(private var current: LocalDateTime) : TimeProvider {
        override fun now(): LocalDateTime = current
        fun setTime(time: LocalDateTime) { current = time }
    }
    
    private val userRepository = TestUserRepository()
    private val threadRepository = TestThreadRepository()
    private val chatRepository = TestChatRepository()
    private val timeProvider = TestTimeProvider(LocalDateTime.of(2025, 9, 10, 12, 0))
    
    private val chatService = ChatService(
        userRepository = userRepository,
        threadRepository = threadRepository,
        chatRepository = chatRepository,
        timeProvider = timeProvider
    )

    class StubPasswordEncoder : PasswordEncoder {
        override fun encode(rawPassword: CharSequence?): String = "encoded-$rawPassword"
        override fun matches(rawPassword: CharSequence?, encodedPassword: String?): Boolean = 
            encodedPassword == encode(rawPassword)
    }

    private fun createUser(): User {
        return User.signUp(
            email = "test@example.com",
            password = "password",
            name = "테스트 사용자",
            role = Role.MEMBER,
            encoder = StubPasswordEncoder(),
            timeProvider = object : TimeProvider {
                override fun now() = LocalDateTime.of(2025, 9, 10, 0, 0)
            }
        )
    }

    @Test
    fun `새 사용자의 첫 메시지는 새 스레드를 생성한다`() {
        // given
        val user = createUser()
        userRepository.addUser(user)
        
        // when
        val result = chatService.sendMessage(
            userEmail = "test@example.com",
            question = "안녕하세요",
            answer = "안녕하세요! 무엇을 도와드릴까요?"
        )
        
        // then
        then(result.question).isEqualTo("안녕하세요")
        then(result.answer).isEqualTo("안녕하세요! 무엇을 도와드릴까요?")
        then(result.threadId).isEqualTo(1L)
    }
    
    @Test
    fun `30분 이내의 스레드가 있으면 기존 스레드를 사용한다`() {
        // given
        val user = createUser()
        userRepository.addUser(user)
        
        // 첫 번째 메시지로 스레드 생성
        chatService.sendMessage("test@example.com", "첫 번째 질문", "첫 번째 답변")
        
        // 20분 후 시간 설정
        timeProvider.setTime(LocalDateTime.of(2025, 9, 10, 12, 20))
        
        // when
        val result = chatService.sendMessage(
            userEmail = "test@example.com", 
            question = "두 번째 질문",
            answer = "두 번째 답변"
        )
        
        // then - 같은 스레드 ID 사용
        then(result.threadId).isEqualTo(1L)
    }
    
    @Test
    fun `30분 이후의 스레드가 있으면 새 스레드를 생성한다`() {
        // given
        val user = createUser()
        userRepository.addUser(user)
        
        // 첫 번째 메시지로 스레드 생성
        chatService.sendMessage("test@example.com", "첫 번째 질문", "첫 번째 답변")
        
        // 31분 후 시간 설정
        timeProvider.setTime(LocalDateTime.of(2025, 9, 10, 12, 31))
        
        // when
        val result = chatService.sendMessage(
            userEmail = "test@example.com",
            question = "새 스레드 질문", 
            answer = "새 스레드 답변"
        )
        
        // then - 새로운 스레드 ID 사용
        then(result.threadId).isEqualTo(2L)
    }
}