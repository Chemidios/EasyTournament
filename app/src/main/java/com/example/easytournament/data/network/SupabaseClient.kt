package com.example.easytournament.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.postgrest.Postgrest

/* Configuración del cliente de red para centralizar la conexión con el BaaS */
object SupabaseClient {
    init {
        /* Debug para comprobar bien la url */
        println("DEBUG_SUPABASE: URL es -> ${BuildConfig.SUPABASE_URL}")
    }
    /* Instancia del cliente
    * Configura la persistencia y la seguridad de la información */
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth) {
            autoLoadFromStorage = true
            sessionManager = SettingsSessionManager()
        }
    }
}
