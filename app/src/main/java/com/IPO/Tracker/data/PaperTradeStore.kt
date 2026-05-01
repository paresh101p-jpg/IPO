package com.IPO.Tracker.data

import android.content.Context
import com.IPO.Tracker.model.IpoData
import org.json.JSONObject
import java.util.Locale

data class PaperTradeRecord(
    val ipoId: String,
    val addedAt: Long
)

data class PaperTradeSummary(
    val selectedCount: Int,
    val openCount: Int,
    val wonCount: Int,
    val lossCount: Int,
    val upcomingCount: Int,
    val missingCount: Int
)

data class PaperTradeDetail(
    val ipoId: String,
    val name: String,
    val status: String,
    val result: String,
    val addedAt: Long
)

object PaperTradeStore {
    private const val PREFS_NAME = "paper_trade_prefs"
    private const val KEY_TRADES = "paper_trade_list"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun serialize(record: PaperTradeRecord): String {
        return JSONObject().put("ipoId", record.ipoId).put("addedAt", record.addedAt).toString()
    }

    private fun parse(raw: String): PaperTradeRecord? {
        return try {
            val json = JSONObject(raw)
            val ipoId = json.optString("ipoId", "")
            if (ipoId.isBlank()) return null
            val addedAt = json.optLong("addedAt", System.currentTimeMillis())
            PaperTradeRecord(ipoId, addedAt)
        } catch (e: Exception) {
            null
        }
    }

    fun getRecords(context: Context): List<PaperTradeRecord> {
        val set = prefs(context).getStringSet(KEY_TRADES, emptySet()) ?: emptySet()
        return set.mapNotNull { parse(it) }.sortedBy { it.addedAt }
    }

    private fun saveRecords(context: Context, records: List<PaperTradeRecord>) {
        val set = records.map { serialize(it) }.toSet()
        prefs(context).edit().putStringSet(KEY_TRADES, set).apply()
    }

    fun isPaperTradeSelected(context: Context, ipoId: String): Boolean {
        return getRecords(context).any { it.ipoId == ipoId }
    }

    fun addPaperTrade(context: Context, ipoId: String) {
        if (isPaperTradeSelected(context, ipoId)) return
        val updated = getRecords(context) + PaperTradeRecord(ipoId, System.currentTimeMillis())
        saveRecords(context, updated)
    }

    fun removePaperTrade(context: Context, ipoId: String) {
        val updated = getRecords(context).filterNot { it.ipoId == ipoId }
        saveRecords(context, updated)
    }

    fun togglePaperTrade(context: Context, ipoId: String): Boolean {
        return if (isPaperTradeSelected(context, ipoId)) {
            removePaperTrade(context, ipoId)
            false
        } else {
            addPaperTrade(context, ipoId)
            true
        }
    }

    private fun parseGmpValue(gmp: String?): Float? {
        if (gmp.isNullOrBlank()) return null
        val cleaned = gmp.replace("₹", "").replace(",", "").replace("%", "").replace("(", " ").replace(")", " ")
        val matches = Regex("-?\\d+(?:\\.\\d+)?").findAll(cleaned).map { it.value }.toList()
        return matches.firstOrNull()?.toFloatOrNull()
    }

    private fun getTradeResult(ipo: IpoData): String {
        val status = ipo.status.trim().lowercase(Locale.getDefault())
        return when {
            status == "open" -> "Open"
            status == "upcoming" -> "Upcoming"
            status == "closed" || status == "listed" -> {
                val value = parseGmpValue(ipo.gmp) ?: 0f
                if (value > 0f) "Won" else "Loss"
            }
            else -> "Pending"
        }
    }

    fun getSummary(context: Context, ipoList: List<IpoData>): PaperTradeSummary {
        val records = getRecords(context)
        val currentIds = ipoList.map { it.id }.toSet()
        val activeRecords = records.filter { it.ipoId in currentIds }
        val activeIpos = ipoList.filter { ipo -> activeRecords.any { it.ipoId == ipo.id } }

        val openCount = activeIpos.count { it.status.equals("Open", true) }
        val upcomingCount = activeIpos.count { it.status.equals("Upcoming", true) }
        val wonCount = activeIpos.count { getTradeResult(it) == "Won" }
        val lossCount = activeIpos.count { getTradeResult(it) == "Loss" }
        val missingCount = records.size - activeRecords.size

        return PaperTradeSummary(
            selectedCount = activeRecords.size,
            openCount = openCount,
            wonCount = wonCount,
            lossCount = lossCount,
            upcomingCount = upcomingCount,
            missingCount = missingCount
        )
    }

    fun getDetails(context: Context, ipoList: List<IpoData>): List<PaperTradeDetail> {
        val records = getRecords(context)
        val ipoMap = ipoList.associateBy { it.id }
        return records.mapNotNull { record ->
            val ipo = ipoMap[record.ipoId] ?: return@mapNotNull null
            PaperTradeDetail(
                ipoId = ipo.id,
                name = ipo.name,
                status = ipo.status,
                result = getTradeResult(ipo),
                addedAt = record.addedAt
            )
        }.sortedByDescending { it.addedAt }
    }
}
