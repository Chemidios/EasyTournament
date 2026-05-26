package com.example.easytournament.data.repository

import android.util.Log
import com.example.easytournament.data.model.Profile
import com.example.easytournament.data.model.Tournament
import com.example.easytournament.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository (){
    private var cachedProfile: Profile? = null
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String, username: String): String? {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("username", username)
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    suspend fun login(email: String, password: String): String? {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }

    fun getCurrentUser(): UserInfo? {
        return client.auth.currentUserOrNull()
    }

    suspend fun getCurrentProfile(): Profile? {
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?: return null
            if (cachedProfile?.id == userId) return cachedProfile

            val profile = client.postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }.decodeSingleOrNull<Profile>()

            cachedProfile = profile
            profile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun logout() {
        try {
            client.auth.signOut()
            cachedProfile = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateProfile(username: String, steam: String, riot: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUser()?.id ?: return@withContext false
                client.postgrest["profiles"].update({
                    set("username", username)
                    set("steam_username", steam)
                    set("riot_username", riot)
                }) {
                    filter { eq("id", userId) }
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getProfileById(userId: String): Profile? {
        return try {
            client.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun enrollInTournament(tournament: Tournament): String? {
        return try {
            val user = client.auth.currentUserOrNull() ?: return "Debes iniciar sesión"

            if (tournament.current_participants >= tournament.max_participants) {
                return "El torneo está lleno"
            }

            if (tournament.status != "abierto") {
                return "Este torneo ya no acepta inscripciones"
            }

            val enrollment = com.example.easytournament.data.model.Enrollment(
                tournament_id = tournament.id!!,
                user_id = user.id
            )

            client.postgrest["enrollments"].insert(enrollment)

            null
        } catch (e: Exception) {
            Log.e("SUPABASE_ERROR", "Detalle: ${e.message}")
            when {
                e.message?.contains("duplicate key", true) == true -> "Ya estás inscrito"
                else -> "Error: ${e.message}"
            }
        }
    }

    suspend fun getMyEnrolledTournaments(): List<Tournament> {
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?: return emptyList()
            val enrollments = client.postgrest["enrollments"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<com.example.easytournament.data.model.Enrollment>()

            val tournamentIds = enrollments.map { it.tournament_id }
            if (tournamentIds.isEmpty()) return emptyList()

            client.postgrest["tournaments"]
                .select { filter { isIn("id", tournamentIds) } }
                .decodeList<Tournament>()
                .sortedBy { it.start_date }
        } catch (e: Exception) {
            emptyList()
        }
    }
}