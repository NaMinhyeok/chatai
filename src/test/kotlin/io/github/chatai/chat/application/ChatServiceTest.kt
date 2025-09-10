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
    
    class TestOpenAIService : OpenAIService {
        override fun generateResponse(messages: List<OpenAIMessage>): String {
            val lastMessage = messages.lastOrNull { it.role == MessageRole.USER }?.content ?: ""
            return "AI 답변: $lastMessage"
        }
    }
    
    private val userRepository = TestUserRepository()
    private val threadRepository = TestThreadRepository()
    private val chatRepository = TestChatRepository()
    private val openAIService = TestOpenAIService()
    private val timeProvider = TestTimeProvider(LocalDateTime.of(2025, 9, 10, 12, 0))
    
    private val chatService = ChatService(
        userRepository = userRepository,
        threadRepository = threadRepository,
        chatRepository = chatRepository,
        openAIService = openAIService,
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
            question = "안녕하세요"
        )
        
        // then
        then(result.question).isEqualTo("안녕하세요")
        then(result.answer).isEqualTo("AI 답변: 안녕하세요")
        then(result.threadId).isEqualTo(1L)
    }
    
    @Test
    fun `30분 이내의 스레드가 있으면 기존 스레드를 사용한다`() {
        // given
        val user = createUser()
        userRepository.addUser(user)
        
        // 첫 번째 메시지로 스레드 생성
        chatService.sendMessage("test@example.com", "첫 번째 질문")
        
        // 20분 후 시간 설정
        timeProvider.setTime(LocalDateTime.of(2025, 9, 10, 12, 20))
        
        // when
        val result = chatService.sendMessage(
            userEmail = "test@example.com", 
            question = "두 번째 질문"
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
        chatService.sendMessage("test@example.com", "첫 번째 질문")
        
        // 31분 후 시간 설정
        timeProvider.setTime(LocalDateTime.of(2025, 9, 10, 12, 31))
        
        // when
        val result = chatService.sendMessage(
            userEmail = "test@example.com",
            question = "새 스레드 질문"
        )
        
        // then - 새로운 스레드 ID 사용
        then(result.threadId).isEqualTo(2L)
    }
    
    @Test
    fun `대화 히스토리가 OpenAI 요청에 포함된다`() {
        // given
        val user = createUser()
        userRepository.addUser(user)
        
        // OpenAI 호출 기록을 확인할 수 있는 테스트용 서비스
        class RecordingOpenAIService : OpenAIService {
            var lastMessages: List<OpenAIMessage> = emptyList()
            
            override fun generateResponse(messages: List<OpenAIMessage>): String {
                lastMessages = messages
                return "테스트 응답"
            }
        }
        
        val recordingService = RecordingOpenAIService()
        val testChatService = ChatService(
            userRepository = userRepository,
            threadRepository = threadRepository, 
            chatRepository = chatRepository,
            openAIService = recordingService,
            timeProvider = timeProvider
        )
        
        // 첫 번째 대화
        testChatService.sendMessage("test@example.com", "첫 번째 질문")
        
        // when - 두 번째 대화
        testChatService.sendMessage("test@example.com", "두 번째 질문")
        
        // then - 히스토리가 포함되어야 함
        val messages = recordingService.lastMessages
        then(messages).hasSize(4) // System + User1 + Assistant1 + User2
        then(messages[0].role).isEqualTo(MessageRole.SYSTEM)
        then(messages[1].role).isEqualTo(MessageRole.USER)
        then(messages[1].content).isEqualTo("첫 번째 질문")
        then(messages[2].role).isEqualTo(MessageRole.ASSISTANT)
        then(messages[3].role).isEqualTo(MessageRole.USER)
        then(messages[3].content).isEqualTo("두 번째 질문")
    }
}