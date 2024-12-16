package com.guguma.guguma_application.network

data class UserResponse(
    val username: String?,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)