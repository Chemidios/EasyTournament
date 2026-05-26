package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String? = null,
    val tournament_id: String,
    val reviewer_id: String,
    val rating: Int,
    val comment: String? = null,
    val created_at: String? = null
)