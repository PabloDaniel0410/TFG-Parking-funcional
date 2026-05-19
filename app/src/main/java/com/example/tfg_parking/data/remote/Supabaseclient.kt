package com.example.tfg_parking.data.remote

import com.example.tfg_parking.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

import io.github.jan.supabase.storage.Storage

object Supabase {
    val client = createSupabaseClient(
        supabaseUrl  = BuildConfig.SUPABASE_URL,
        supabaseKey  = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)

    }
}