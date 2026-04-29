package com.IPO.Tracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.IPO.Tracker.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val apiService = ApiService.create()
            val ipos = apiService.getIpos(System.currentTimeMillis())
            
            // Check for new IPOs
            val prefs = context.getSharedPreferences("ipo_prefs", Context.MODE_PRIVATE)
            val lastIpoCount = prefs.getInt("last_ipo_count", 0)
            
            if (ipos.size > lastIpoCount && lastIpoCount != 0) {
                val newIpo = ipos.firstOrNull()
                sendNotification("New IPO Alert! 🚀", "${newIpo?.name ?: "New IPO"} is now available.")
            }
            
            prefs.edit().putInt("last_ipo_count", ipos.size).apply()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ipo_updates"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "IPO Updates", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1, notification)
    }
}
