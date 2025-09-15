package io.github.chatai.chat.infrastructure

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

@Configuration
class OpenAIConfiguration {

    @Bean
    @ConditionalOnBean(name = ["clientOpenAIConfig"])
    fun openAIRestClient(@Qualifier("clientOpenAIConfig") openAIConfig: OpenAIConfig): RestClient {
        return RestClient.builder()
            .baseUrl(openAIConfig.baseUrl)
            .defaultHeader("Authorization", "Bearer ${openAIConfig.apiKey}")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    @Bean
    @ConditionalOnBean(name = ["openAIConfig"])
    fun defaultOpenAIRestClient(openAIConfig: OpenAIConfig): RestClient {
        return RestClient.builder()
            .baseUrl(openAIConfig.baseUrl)
            .defaultHeader("Authorization", "Bearer ${openAIConfig.apiKey}")
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}