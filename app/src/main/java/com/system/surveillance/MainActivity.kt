package com.system.surveillance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

class MainActivity : AppCompatActivity() {
    private lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.activity_list_item)

        // Initialize Supabase client with your URL and API key
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://ubixfkksdpzmiixynvqq.supabase.co", // e.g., https://your-project-id.supabase.co
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViaXhma2tzZHB6bWlpeHludnFxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NDE3NTUsImV4cCI6MjA2MjExNzc1NX0.yT7aGQvAsYvb9qB2ZiEeK8edeXzs47d0eY94VdfWylc"  // e.g., eyJhb...
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
