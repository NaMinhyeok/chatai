package io.github.chatai.chat.infrastructure

import io.github.chatai.chat.application.MessageRole
import io.github.chatai.chat.application.OpenAIMessage
import io.github.chatai.chat.application.OpenAIService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 실제 OpenAI API를 호출하는 구현체
 * production 프로필에서 사용
 */
@Service
@Profile("production")
class OpenAIServiceImpl(
    @Value("\${openai.api-key}") private val apiKey: String,
    @Value("\${openai.model:gpt-4}") private val model: String,
    private val restTemplate: RestTemplate = RestTemplate()
) : OpenAIService {
    
    private val logger = LoggerFactory.getLogger(OpenAIServiceImpl::class.java)
    private val openaiUrl = "https://api.openai.com/v1/chat/completions"

    override fun generateResponse(messages: List<OpenAIMessage>): String {
        logger.info("OpenAI API 호출 시작 - model: $model, messages: ${messages.size}개")
        
        try {
            // HTTP 헤더 설정
            val headers = HttpHeaders().apply {
                set("Content-Type", "application/json")
                set("Authorization", "Bearer $apiKey")
            }
            
            // OpenAI API 요청 바디 구성
            val requestBody = mapOf(
                "model" to model,
                "messages" to messages.map { message ->
                    mapOf(
                        "role" to when(message.role) {
                            MessageRole.USER -> "user"
                            MessageRole.ASSISTANT -> "assistant"
                            MessageRole.SYSTEM -> "system"
                        },
                        "content" to message.content
                    )
                }
            )
            
            val requestEntity = HttpEntity(requestBody, headers)
            
            // OpenAI API 호출
            val response = restTemplate.exchange(
                openaiUrl,
                HttpMethod.POST,
                requestEntity,
                Map::class.java
            )
            
            // 응답 파싱
            val responseBody = response.body as? Map<String, Any>
                ?: throw RuntimeException("OpenAI API 응답이 비어있습니다")
            
            val choices = responseBody["choices"] as? List<Map<String, Any>>
                ?: throw RuntimeException("OpenAI API 응답 형식이 올바르지 않습니다")
            
            val firstChoice = choices.firstOrNull() as? Map<String, Any>
                ?: throw RuntimeException("OpenAI API 응답에 choices가 없습니다")
            
            val message = firstChoice["message"] as? Map<String, Any>
                ?: throw RuntimeException("OpenAI API 응답에 message가 없습니다")
            
            val content = message["content"] as? String
                ?: throw RuntimeException("OpenAI API 응답에 content가 없습니다")
            
            logger.info("OpenAI API 호출 성공 - 응답 길이: ${content.length}")
            return content
            
        } catch (e: Exception) {
            logger.error("OpenAI API 호출 실패", e)
            throw RuntimeException("AI 응답 생성에 실패했습니다: ${e.message}")
        }
    }
}