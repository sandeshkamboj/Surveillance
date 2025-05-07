package com.system.surveillance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://ubixfkksdpzmiixynvqq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViaXhma2tzZHB6bWlpeHludnFxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NDE3NTUsImV4cCI6MjA2MjExNzc1NX0.yT7aGQvAsYvb9qB2ZiEeK8edeXzs47d0eY94VdfWylc"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabaseClient.postgrest.from("devices").insert(
                    mapOf(
                        "device_id" to "12345",
                        "status" to "active"
                    )
                )
                println("Insert result: $result")
            } catch (e: Exception) {
                println("Error: $e")
            }
        }
    }
}
