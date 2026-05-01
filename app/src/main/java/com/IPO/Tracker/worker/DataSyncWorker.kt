package com.IPO.Tracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.IPO.Tracker.data.NotificationPreferencesStore
import com.IPO.Tracker.model.IpoData
import com.IPO.Tracker.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

class DataSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val apiService = ApiService.create()
            val ipos = apiService.getIpos()
            val news = apiService.getNews()

            val prefs = context.getSharedPreferences("ipo_prefs", Context.MODE_PRIVATE)
            val lastIpoCount = prefs.getInt("last_ipo_count", 0)
            if (ipos.size > lastIpoCount && lastIpoCount != 0) {
                val newIpo = ipos.firstOrNull()
                sendNotification("New IPO Alert! 🚀", "${newIpo?.name ?: "New IPO"} is now available.")
            }
            prefs.edit().putInt("last_ipo_count", ipos.size).apply()

            val selectedIpoIds = NotificationPreferencesStore.getIpoNotificationIds(context)
            val listingEnabled = NotificationPreferencesStore.isListingAlertEnabled(context)
            val allotmentEnabled = NotificationPreferencesStore.isAllotmentAlertEnabled(context)
            val gmpEnabled = NotificationPreferencesStore.isGmpAlertEnabled(context)
            val newsEnabled = NotificationPreferencesStore.isNewsAlertEnabled(context)

            val previousSnapshots = loadIpoSnapshots()
            val previousNewsSet = loadNewsSnapshot()
            val newNewsItems = news.filter { !previousNewsSet.contains(it.id + "|" + it.date) }

            ipos.forEach { ipo ->
                if (!selectedIpoIds.contains(ipo.id)) return@forEach
                val previous = previousSnapshots[ipo.id]
                if (previous != null) {
                    if (listingEnabled && previous.listingDate != ipo.listingDate) {
                        sendNotification("IPO Listing Updated", "${ipo.name} listing date changed to ${ipo.listingDate ?: "TBD"}.")
                    }
                    if (allotmentEnabled && previous.status != ipo.status) {
                        sendNotification("IPO Status Updated", "${ipo.name} status changed from ${previous.status} to ${ipo.status}.")
                    }
                    if (gmpEnabled && previous.gmp != ipo.gmp) {
                        sendNotification("GMP Update for ${ipo.name}", "New GMP: ${ipo.gmp} (was ${previous.gmp}).")
                    }
                }

                if (newsEnabled && newNewsItems.any { it.headline.contains(ipo.name, true) || it.summary.contains(ipo.name, true) }) {
                    sendNotification("IPO News: ${ipo.name}", "New news related to ${ipo.name}! Check the News tab.")
                }
            }

            saveIpoSnapshots(ipos)
            saveNewsSnapshot(news)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun loadIpoSnapshots(): Map<String, IpoSnapshot> {
        val prefs = context.getSharedPreferences("notification_snapshots", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("ipo_snapshot", emptySet()) ?: emptySet()
        return set.mapNotNull { parseSnapshot(it) }.associateBy { it.id }
    }

    private fun saveIpoSnapshots(ipos: List<IpoData>) {
        val prefs = context.getSharedPreferences("notification_snapshots", Context.MODE_PRIVATE)
        val set = ipos.map { snapshotToString(it) }.toSet()
        prefs.edit().putStringSet("ipo_snapshot", set).apply()
    }

    private fun loadNewsSnapshot(): Set<String> {
        val prefs = context.getSharedPreferences("notification_snapshots", Context.MODE_PRIVATE)
        return prefs.getStringSet("news_snapshot", emptySet()) ?: emptySet()
    }

    private fun saveNewsSnapshot(news: List<com.IPO.Tracker.model.NewsData>) {
        val prefs = context.getSharedPreferences("notification_snapshots", Context.MODE_PRIVATE)
        val set = news.map { it.id + "|" + it.date }.toSet()
        prefs.edit().putStringSet("news_snapshot", set).apply()
    }

    private fun snapshotToString(ipo: IpoData): String {
        return JSONObject()
            .put("id", ipo.id)
            .put("name", ipo.name)
            .put("gmp", ipo.gmp)
            .put("openDate", ipo.openDate ?: "")
            .put("closeDate", ipo.closeDate ?: "")
            .put("listingDate", ipo.listingDate ?: "")
            .put("status", ipo.status)
            .toString()
    }

    private fun parseSnapshot(raw: String): IpoSnapshot? {
        return try {
            val json = JSONObject(raw)
            IpoSnapshot(
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                gmp = json.optString("gmp", ""),
                openDate = json.optString("openDate", ""),
                closeDate = json.optString("closeDate", ""),
                listingDate = json.optString("listingDate", ""),
                status = json.optString("status", "")
            ).takeIf { it.id.isNotBlank() }
        } catch (e: Exception) {
            null
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

        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        notificationManager.notify(id, notification)
    }

    private data class IpoSnapshot(
        val id: String,
        val name: String,
        val gmp: String,
        val openDate: String,
        val closeDate: String,
        val listingDate: String,
        val status: String
    )
}
