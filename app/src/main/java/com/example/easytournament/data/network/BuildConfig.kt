package com.example.easytournament.data.network

/* Configuración de las variables globales de entorno para el cliente de red */
object BuildConfig {

    /* Endpoint de la API REST de Supabase */
    const val SUPABASE_URL: String = "https://iytyqsdxwbjhyghxoctn.supabase.co"

    /*Llave pública que permite la comunicación con el Gateway de Supabase.
      La seguridad real se delega en las políticas RLS (Row Level Security)
      definidas en la base de datos SQL.*/
    const val SUPABASE_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml5dHlxc2R4d2JqaHlnaHhvY3RuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU1OTI4NDYsImV4cCI6MjA5MTE2ODg0Nn0.op5mxj6OmP-8mSw4eRxrkQma2H45IPfsovoXLzusdkw"
}