package com.example.easytournament.data.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.postgrest.Postgrest
object SupabaseClient {
    init {
        println("DEBUG_SUPABASE: URL es -> ${BuildConfig.SUPABASE_URL}")
    }
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
