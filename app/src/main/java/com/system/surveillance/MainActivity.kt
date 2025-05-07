package com.system.surveillance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Supabase client
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://ubixfkksdpzmiixynvqq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViaXhma2tzZHB6bWlpe>
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }

        // Example: Insert a device record into the 'devices' table
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabaseClient.postgrest.from("devices").insert(
                    mapOf(
                        "device_id" to android.os.Build.SERIAL,
                        "name" to android.os.Build.MODEL
                    )
                )
                println("Inserted device: $result")
            } catch (e: Exception) {
                println("Error inserting device: $e")
            }
        }
    }
}
