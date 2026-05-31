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

/* Repositorio de autenticación y perfil */
class AuthRepository (){

    /* Caché en memoria para optimizar el rendimiento y reducir peticiones de red */
    private var cachedProfile: Profile? = null
    private val client = SupabaseClient.client

    /* Gestión de registro */
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

    /* Gestión de inicio de sesión */
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

    /* Consulta y persistencia de perfiles de usuario */
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

    /* Función de cierre de sesión */
    suspend fun logout() {
        try {
            client.auth.signOut()
            cachedProfile = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /* Actualización de perfil */
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

    /* Obtención de perfil por ID */
    suspend fun getProfileById(userId: String): Profile? {
        return try {
            client.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }
}