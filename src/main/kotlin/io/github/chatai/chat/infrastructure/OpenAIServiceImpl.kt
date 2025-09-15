package io.github.chatai.chat.infrastructure

import io.github.chatai.chat.application.MessageRole
import io.github.chatai.chat.application.OpenAIMessage
import io.github.chatai.chat.application.OpenAIService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

/**
 * 실제 OpenAI API를 호출하는 구현체
 * production 프로필에서 사용
 */
@Service
@Profile("production")
class OpenAIServiceImpl(
    private val openAIConfig: OpenAIConfig,
    private val restClient: RestClient
) : OpenAIService {

    private val logger = LoggerFactory.getLogger(OpenAIServiceImpl::class.java)

    override fun generateResponse(messages: List<OpenAIMessage>): String {
        logger.info("OpenAI API 호출 시작 - model: ${openAIConfig.model}, messages: ${messages.size}개")

        try {
            // OpenAI API 요청 바디 구성
            val requestBody = mapOf(
                "model" to openAIConfig.model,
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

            // OpenAI API 호출
            val response = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(Map::class.java) as? Map<String, Any>
                ?: throw RuntimeException("OpenAI API 응답이 비어있습니다")

            // 응답 파싱
            val choices = response["choices"] as? List<Map<String, Any>>
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