package io.github.chatai.chat.infrastructure

import io.github.chatai.chat.application.OpenAIMessage
import io.github.chatai.chat.application.OpenAIService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

/**
 * 실제 OpenAI API를 호출하는 구현체
 * production 프로필에서 사용
 */
@Service
@Profile("production")
class OpenAIServiceImpl(
    @Value("\${openai.api-key}") private val apiKey: String,
    @Value("\${openai.model:gpt-3.5-turbo}") private val model: String
) : OpenAIService {
    
    private val logger = LoggerFactory.getLogger(OpenAIServiceImpl::class.java)

    override fun generateResponse(messages: List<OpenAIMessage>): String {
        logger.info("OpenAI API 호출 시작 - model: $model, messages: ${messages.size}개")
        
        // TODO: 실제 OpenAI API 호출 구현
        // 예시:
        // val request = ChatCompletionRequest.builder()
        //     .model(model)
        //     .messages(messages.map { it.toOpenAIMessage() })
        //     .build()
        // 
        // val response = openAIClient.createChatCompletion(request)
        // return response.choices[0].message.content
        
        throw NotImplementedError("실제 OpenAI API 연동은 향후 구현 예정입니다.")
    }
}