package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String,
    val steam_username: String? = null,
    val riot_username: String? = null,
    val reputation: Float = 0f,
    val is_admin: Boolean = false,
    val avatar_url: String? = null
)