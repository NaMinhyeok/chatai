package io.github.chatai.config

import io.github.chatai.chat.infrastructure.OpenAIConfig
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment

/**
 * 클라이언트별 설정을 부트스트래핑 시점에 동적으로 등록하는 초기화 클래스
 */
class ClientConfigInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    private val logger = LoggerFactory.getLogger(ClientConfigInitializer::class.java)

    override fun initialize(context: ConfigurableApplicationContext) {
        val environment = context.environment
        val clientName = environment.getProperty("client.name", "default")

        logger.info("클라이언트별 설정 초기화 시작 - client: $clientName")

        val openAIConfig = createClientSpecificConfig(clientName, environment)

        // 동적으로 OpenAI 설정 빈 등록 (기존 빈을 덮어씀)
        context.beanFactory.registerSingleton("clientOpenAIConfig", openAIConfig)

        logger.info("클라이언트별 OpenAI 설정 등록 완료 - model: ${openAIConfig.model}")

        // 클라이언트별 추가 설정이 필요한 경우 여기서 처리
        registerAdditionalBeans(clientName, context)
    }

    private fun createClientSpecificConfig(clientName: String, environment: Environment): OpenAIConfig {
        return when (clientName.lowercase()) {
            "samsung" -> OpenAIConfig(
                apiKey = environment.getProperty("clients.samsung.openai.api-key", "samsung-default-key"),
                model = environment.getProperty("clients.samsung.openai.model", "gpt-4-turbo"),
                baseUrl = environment.getProperty("clients.samsung.openai.base-url", "https://api.openai.com/v1/chat/completions")
            )

            "lg" -> OpenAIConfig(
                apiKey = environment.getProperty("clients.lg.openai.api-key", "lg-default-key"),
                model = environment.getProperty("clients.lg.openai.model", "gpt-3.5-turbo"),
                baseUrl = environment.getProperty("clients.lg.openai.base-url", "https://api.openai.com/v1/chat/completions")
            )

            "sk" -> OpenAIConfig(
                apiKey = environment.getProperty("clients.sk.openai.api-key", "sk-default-key"),
                model = environment.getProperty("clients.sk.openai.model", "gpt-4"),
                baseUrl = environment.getProperty("clients.sk.openai.base-url", "https://api.openai.com/v1/chat/completions")
            )

            else -> OpenAIConfig(
                apiKey = environment.getProperty("openai.api-key", "default-key"),
                model = environment.getProperty("openai.model", "gpt-4"),
                baseUrl = environment.getProperty("openai.base-url", "https://api.openai.com/v1/chat/completions")
            )
        }
    }

    private fun registerAdditionalBeans(clientName: String, context: ConfigurableApplicationContext) {
        // 클라이언트별 특별한 빈이 필요한 경우 여기서 등록
        when (clientName.lowercase()) {
            "samsung" -> {
                // 삼성 전용 추가 설정
                logger.debug("삼성 클라이언트 전용 설정 적용")
            }
            "lg" -> {
                // LG 전용 추가 설정
                logger.debug("LG 클라이언트 전용 설정 적용")
            }
        }
    }
}