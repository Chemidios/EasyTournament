package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

/* Modelo de Review
* Se usa Serializable para poder convertirlo a JSON y vicebersa de manera rápida */
@Serializable
data class Review(
    /* Id único de Review generado por la base de datos */
    val id: String? = null,
    /* FK que vincula una review a un torneo y usuario especificos */
    val tournament_id: String,
    val reviewer_id: String,
    /* Puntuación del torneo */
    val rating: Int,
    /* Comentarios del torneo */
    val comment: String? = null,
    val created_at: String? = null
)