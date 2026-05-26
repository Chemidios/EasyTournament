package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Tournament(
    val id: String? = null,
    val creator_id: String,
    val title: String,
    val description: String? = null,
    val game_name: String,
    val max_participants: Int,
    val current_participants: Int = 0,
    val status: String = "abierto",
    val start_date: String,
    val category: String = "Otros",
    val modality: String = "Por Determinar",
    val rating_avg: Float = 0f,
    val profiles: Profile? = null
)