package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

/* Modelo de Inscripción
* Se usa Serializable para poder convertirlo a JSON y vicebersa de manera rápida */
@Serializable
data class Enrollment(
    /* Id único de inscripción generado por la base de datos */
    val id: String? = null,
    /* FK que vincula una inscripcion a un torneo y usuario especificos */
    val tournament_id: String,
    val user_id: String,
    /* Fecha de registro generada */
    val enrolled_at: String? = null
)