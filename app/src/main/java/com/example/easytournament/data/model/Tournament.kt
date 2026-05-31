package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

/* Modelo de torneo
* Se usa Serializable para poder convertirlo a JSON y vicebersa de manera rápida */
@Serializable
data class Tournament(
    /* Id generado por la base de datos tipo UUID */
    val id: String? = null,
    /* Clave foránea vinculada al id del creador en tabla Profiles */
    val creator_id: String,
    /* Datos básicos del torneo */
    val title: String,
    val description: String? = null,
    val game_name: String,
    /* Control de número y número máximo de participantes */
    val max_participants: Int,
    val current_participants: Int = 0,
    /* Control de estado de torneo */
    val status: String = "abierto",
    /* Control de torneo en fecha */
    val start_date: String,
    /* Categorías y modalidades de los torneos */
    val category: String = "Otros",
    val modality: String = "Por Determinar",
    /* Media de valoraciones */
    val rating_avg: Float = 0f,
    /* Contiene información de perfiles */
    val profiles: Profile? = null
)