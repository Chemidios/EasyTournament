package com.example.easytournament.data.repository

import com.example.easytournament.data.model.Profile
import com.example.easytournament.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable


/* Repositorio encargado de la gestión de perfiles y usuariosc*/
class UserRepository {

    /* Cliente de Supabase configurado centralizadamente en el objeto SupabaseClient */
    private val client = SupabaseClient.client

    /* Funcion para obtener todos los perfiles en formato JSON y convertirlos en Profile */
    suspend fun getAllProfiles(): List<Profile> {
        return try {
            client.from("profiles").select().decodeList<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    /* Función para cambiar la reputación de un usuario mediante parametros recibidos */
    suspend fun updateReputation(userId: String, amount: Float): Boolean {
        return try {
            client.postgrest.rpc(
                function = "update_user_reputation",
                parameters = ReputationParams(
                    user_uuid = userId,
                    amount = amount
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/* Clase exclusiva para cambio de parametros */
@Serializable
data class ReputationParams(
    val user_uuid: String,
    val amount: Float
)