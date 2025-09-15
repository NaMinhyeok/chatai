package io.github.chatai.chat.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "openai")
data class OpenAIConfig(
    var apiKey: String = "",
    var model: String = "gpt-4",
    var baseUrl: String = "https://api.openai.com/v1/chat/completions"
)