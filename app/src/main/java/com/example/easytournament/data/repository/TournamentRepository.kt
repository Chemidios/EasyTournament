package com.example.easytournament.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.easytournament.data.model.Enrollment
import com.example.easytournament.data.model.Profile
import com.example.easytournament.data.model.Tournament
import com.example.easytournament.data.model.Review
import com.example.easytournament.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit


class TournamentRepository {
    private val client = SupabaseClient.client
    private val userRepo = UserRepository()

    suspend fun getAllTournaments(): List<Tournament> {
        return try {
            val result = client.from("tournaments")
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, profiles(*)"))
                .decodeList<Tournament>()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun unenrollFromTournament(tournament: Tournament): String? {
        return try {
            val user = client.auth.currentUserOrNull() ?: return "Sesión expirada"
            val userId = user.id

            val today = LocalDate.now()
            val tournamentDate = ZonedDateTime.parse(tournament.start_date).toLocalDate()

            val daysUntil = ChronoUnit.DAYS.between(today, tournamentDate)
            if (daysUntil <= 1) {
                userRepo.updateReputation(userId, 0.5f)
            }

            client.postgrest["enrollments"].delete {
                filter {
                    eq("tournament_id", tournament.id!!)
                    eq("user_id", userId)
                }
            }
            null
        } catch (e: Exception) {
            e.message
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun deleteTournament(tournament: Tournament, isAdmin: Boolean): String? {
        return try {
            val userId = client.auth.currentUserOrNull()?.id ?: return "Sesión expirada"

            if (!isAdmin) {
                val today = LocalDate.now()

                println("DEBUG_PENALTY: Hoy es $today. Fecha torneo recibida: ${tournament.start_date}")

                val tournamentDate = try {
                    ZonedDateTime.parse(tournament.start_date).toLocalDate()
                } catch (e: Exception) {
                    println("DEBUG_PENALTY: Fallo parseo ZonedDateTime, intentando LocalDate")
                    LocalDate.parse(tournament.start_date.split("T")[0])
                }

                val daysUntil = ChronoUnit.DAYS.between(today, tournamentDate)
                val limitParticipants = tournament.max_participants / 2

                println("DEBUG_PENALTY: Días restantes: $daysUntil")
                println("DEBUG_PENALTY: Participantes actuales: ${tournament.current_participants} / Límite para penalizar: $limitParticipants")

                if (daysUntil <= 1 || tournament.current_participants > limitParticipants) {
                    println("DEBUG_PENALTY: Condición de penalización CUMPLIDA. Llamando a penalizeUser...")
                    userRepo.updateReputation(userId, 1.0f)
                } else {
                    println("DEBUG_PENALTY: Condición NO cumplida. No se penaliza.")
                }
            } else {
                println("DEBUG_PENALTY: Es ADMIN, saltando lógica de penalización.")
            }

            client.from("tournaments").delete {
                filter { eq("id", tournament.id!!) }
            }
            null
        } catch (e: Exception) {
            println("DEBUG_PENALTY: Error crítico en deleteTournament: ${e.message}")
            e.message
        }
    }

    suspend fun createTournament(tournament: Tournament): String? {
        return try {
            client.from("tournaments").insert(tournament)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            "Error al crear el torneo: ${e.message}"
        }
    }

    suspend fun updateTournamentStatus(tournamentId: String, newStatus: String) {
        try {
            // Usamos .from("tabla") y el método update con sintaxis de Mapa/Lambda
            client.from("tournaments").update(
                {
                    // "status" es el nombre de la columna en tu SQL de Supabase
                    set("status", newStatus)
                }
            ) {
                filter {
                    // "id" es el nombre de la columna identificadora
                    eq("id", tournamentId)
                }
            }
            println("DEBUG_STATUS: Torneo $tournamentId actualizado a $newStatus en BDD")
        } catch (e: Exception) {
            println("DEBUG_STATUS: Error al actualizar BDD: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun addReview(review: Review): String? {
        return try {
            client.from("reviews").insert(review)
            null // Éxito
        } catch (e: Exception) {
            e.message
        }
    }

    suspend fun getTournamentReviews(tournamentId: String): List<Review> {
        return try {
            client.from("reviews").select {
                filter { eq("tournament_id", tournamentId) }
            }.decodeList<Review>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getParticipantsIds(tournamentId: String): List<String> {
        return try {
            val result = client.from("enrollments")
                .select {
                    filter { eq("tournament_id", tournamentId) }
                }
                .decodeList<Enrollment>()

            // Esto devuelve la lista de IDs de TODOS los inscritos
            result.map { it.user_id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUsernameById(userId: String): String {
        return try {
            val profile = SupabaseClient.client.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingle<Profile>()
            profile.username
        } catch (e: Exception) {
            "Usuario"
        }
    }

}