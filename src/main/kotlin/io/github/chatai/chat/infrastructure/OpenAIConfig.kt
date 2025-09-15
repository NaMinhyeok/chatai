package io.github.chatai.chat.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "openai")
data class OpenAIConfig @ConstructorBinding constructor(
    val apiKey: String,
    val model: String = "gpt-4",
    val baseUrl: String = "https://api.openai.com/v1/chat/completions"
)