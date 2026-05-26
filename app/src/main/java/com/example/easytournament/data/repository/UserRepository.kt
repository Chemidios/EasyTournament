package com.example.easytournament.data.repository

import com.example.easytournament.data.model.Profile
import com.example.easytournament.data.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable



class UserRepository {
    private val client = SupabaseClient.client

    suspend fun getAllProfiles(): List<Profile> {
        return try {
            client.from("profiles").select().decodeList<Profile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateReputation(userId: String, amount: Float): Boolean {
        return try {
            client.postgrest.rpc(
                function = "subtract_reputation",
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

@Serializable
data class ReputationParams(
    val user_uuid: String,
    val amount: Float
)