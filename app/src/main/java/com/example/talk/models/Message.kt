package com.example.talk.models

data class Message(
    private val messageId: String? = null,
    var message: String? = null,
    var senderId: String? = null,
    val imageUrl: String? = null,
    var timestamp: Long? = null
) {}

