package com.example.easytournament.data.model

import kotlinx.serialization.Serializable

/* Modelo de perfil
* Se usa Serializable para poder convertirlo a JSON y vicebersa de manera rápida */
@Serializable
data class Profile(
    /* UUID unico vinculado a la cuenta de Supabase y autenticación */
    val id: String,
    val username: String,
    /* Campos opcionales para integrar plataformas externas */
    val steam_username: String? = null,
    val riot_username: String? = null,
    /* Controla la reputación del usuario */
    val reputation: Float = 0f,
    /* Control para privilegios de administracion */
    val is_admin: Boolean = false,

    /* Url de avatar. No implementado*/
    val avatar_url: String? = null
)