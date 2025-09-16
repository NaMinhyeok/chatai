package io.github.chatai.chat.infrastructure

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
class OpenAIConfiguration {

    @Bean
    @ConditionalOnProperty(name = ["client.name"], havingValue = "samsung")
    fun samsungOpenAIConfig(environment: Environment): OpenAIConfig {
        return createClientConfig("samsung", "gpt-4-turbo", environment)
    }

    @Bean
    @ConditionalOnProperty(name = ["client.name"], havingValue = "lg")
    fun lgOpenAIConfig(environment: Environment): OpenAIConfig {
        return createClientConfig("lg", "gpt-3.5-turbo", environment)
    }

    @Bean
    @ConditionalOnProperty(name = ["client.name"], havingValue = "sk")
    fun skOpenAIConfig(environment: Environment): OpenAIConfig {
        return createClientConfig("sk", "gpt-4", environment)
    }

    @Bean
    @ConditionalOnMissingBean
    fun defaultOpenAIConfig(environment: Environment): OpenAIConfig {
        return OpenAIConfig(
            apiKey = environment.getProperty("openai.api-key", "mock-api-key"),
            model = environment.getProperty("openai.model", "gpt-4"),
            baseUrl = environment.getProperty("openai.base-url", "https://api.openai.com/v1/chat/completions")
        )
    }

    @Bean
    fun openAIRestClient(openAIConfig: OpenAIConfig): RestClient {
        return RestClient.builder()
            .baseUrl(openAIConfig.baseUrl)
            .defaultHeader("Authorization", "Bearer ${openAIConfig.apiKey}")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    private fun createClientConfig(clientName: String, model: String, environment: Environment): OpenAIConfig {
        return OpenAIConfig(
            apiKey = environment.getProperty("clients.$clientName.openai.api-key",
                System.getenv("${clientName.uppercase()}_OPENAI_API_KEY") ?: "$clientName-mock-key"),
            model = environment.getProperty("clients.$clientName.openai.model", model),
            baseUrl = environment.getProperty("clients.$clientName.openai.base-url",
                "https://api.openai.com/v1/chat/completions")
        )
    }
}