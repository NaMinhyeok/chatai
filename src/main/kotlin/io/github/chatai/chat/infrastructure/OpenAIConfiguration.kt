package io.github.chatai.chat.infrastructure

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(OpenAIConfig::class)
class OpenAIConfiguration {

    @Bean
    fun openAIRestClient(openAIConfig: OpenAIConfig): RestClient {
        return RestClient.builder()
            .baseUrl(openAIConfig.baseUrl)
            .defaultHeader("Authorization", "Bearer ${openAIConfig.apiKey}")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}