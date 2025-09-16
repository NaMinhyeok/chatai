package io.github.chatai.chat.infrastructure

data class OpenAIConfig(
    var apiKey: String = "",
    var model: String = "gpt-4",
    var baseUrl: String = "https://api.openai.com/v1/chat/completions"
)