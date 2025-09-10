package io.github.chatai.chat.infrastructure

import io.github.chatai.chat.application.MessageRole
import io.github.chatai.chat.application.OpenAIMessage
import io.github.chatai.chat.application.OpenAIService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * 개발 및 테스트용 OpenAI API Mock 구현체
 * default, dev, test 프로필에서 사용
 */
@Service
@Profile("!production")
class MockOpenAIService : OpenAIService {
    
    private val logger = LoggerFactory.getLogger(MockOpenAIService::class.java)
    
    override fun generateResponse(messages: List<OpenAIMessage>): String {
        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }?.content
        
        logger.debug("Mock OpenAI API 호출 - 사용자 메시지: $lastUserMessage")
        
        return when {
            lastUserMessage == null -> "죄송합니다. 질문을 이해하지 못했습니다."
            
            lastUserMessage.contains("안녕") || lastUserMessage.contains("hello", ignoreCase = true) -> 
                "안녕하세요! 저는 AI 어시스턴트입니다. 무엇을 도와드릴까요?"
                
            lastUserMessage.contains("이름") -> 
                "저는 ChatAI입니다. VIP onboarding 팀에서 개발한 AI 어시스턴트예요."
                
            lastUserMessage.contains("날씨") -> 
                "죄송하지만 실시간 날씨 정보는 제공할 수 없습니다. 날씨 앱이나 웹사이트를 확인해 보세요."
                
            lastUserMessage.contains("시간") -> 
                "현재 시간 정보는 제공할 수 없지만, 시계나 디바이스의 시간을 확인해 보세요."
                
            lastUserMessage.contains("도움") || lastUserMessage.contains("help", ignoreCase = true) -> 
                "네! 저는 다양한 질문에 답변드릴 수 있습니다. 궁금한 것이 있으시면 언제든 물어보세요."
                
            lastUserMessage.length < 5 -> 
                "조금 더 구체적으로 질문해 주시면 더 나은 답변을 드릴 수 있어요."
                
            else -> 
                "흥미로운 질문이네요! 실제 서비스에서는 OpenAI가 더 상세한 답변을 제공할 것입니다. " +
                "현재는 개발용 Mock 응답입니다."
        }.also {
            logger.debug("Mock 응답 생성: $it")
        }
    }
}