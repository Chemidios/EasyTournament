package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Enrollment(
    val id: String? = null,
    val tournament_id: String,
    val user_id: String,
    val enrolled_at: String? = null
)