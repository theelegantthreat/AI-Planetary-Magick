package com.example.ai

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val groundingType: GroundingType = GroundingType.NONE,
    val searchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList()
)

enum class MessageSender {
    USER,
    AI_GUIDE
}

enum class GroundingType {
    NONE,
    SEARCH,
    MAPS,
    BOTH
}

data class GroundingSource(
    val title: String,
    val uri: String
)
