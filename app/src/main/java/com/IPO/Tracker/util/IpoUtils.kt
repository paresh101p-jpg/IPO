package com.IPO.Tracker.util

import com.IPO.Tracker.model.IpoData
import java.text.SimpleDateFormat
import java.util.*

fun String?.normalizeString(): String = this?.trim()?.lowercase(Locale.getDefault()) ?: ""

fun parseDate(dateStr: String?): Date? {
    if (dateStr.isNullOrEmpty()) return null
    val formats = listOf(
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH)
    )
    for (fmt in formats) {
        try { val d = fmt.parse(dateStr); if (d != null) return d } catch (e: Exception) { }
    }
    return null
}

fun inferIpoStatus(ipo: IpoData): String {
    val now = System.currentTimeMillis()
    val openDate = parseDate(ipo.openDate)
    val closeDate = parseDate(ipo.closeDate)

    if (openDate != null && closeDate != null) {
        val closeEndOfDay = closeDate.time + (24 * 60 * 60 * 1000) - 1
        if (now in openDate.time..closeEndOfDay) return "Open"
        if (now < openDate.time) return "Upcoming"
        if (now > closeEndOfDay) return "Closed"
    }

    if (openDate != null && now < openDate.time) return "Upcoming"
    if (closeDate != null && now > closeDate.time) return "Closed"

    return when (ipo.status.normalizeString()) {
        "open" -> "Open"
        "upcoming" -> "Upcoming"
        "closed", "listed" -> "Closed"
        else -> ipo.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

fun extractNumbers(text: String?): List<Int> {
    if (text == null) return emptyList()
    // Remove percentages and anything in brackets to avoid extracting those numbers
    val sanitized = text.replace(Regex("\\(.*?\\)"), "").replace("%", "")
    return Regex("\\d+").findAll(sanitized.replace(",", ""))
        .mapNotNull { it.value.toIntOrNull() }
        .toList()
}

fun parseFirstFloat(text: String?): Float {
    if (text == null) return 0f
    // Extract the first number (including decimal) before any brackets or percentage
    val sanitized = text.replace(Regex("\\(.*?\\)"), "").replace("%", "")
    val match = Regex("(\\d+\\.?\\d*)").find(sanitized.replace(",", ""))
    return match?.value?.toFloatOrNull() ?: 0f
}

fun formatTotalAmount(priceStr: String, lotSizeStr: String?): String {
    val priceNumbers = extractNumbers(priceStr)
    val lotSize = lotSizeStr?.let { extractNumbers(it).firstOrNull() } ?: 0
    if (priceNumbers.isEmpty() || lotSize <= 0) return "TBD"

    return if (priceNumbers.size == 1) {
        "₹${priceNumbers[0] * lotSize}"
    } else {
        val minTotal = priceNumbers.minOrNull()!! * lotSize
        val maxTotal = priceNumbers.maxOrNull()!! * lotSize
        if (minTotal == maxTotal) "₹$minTotal" else "₹${minTotal} - ₹${maxTotal}"
    }
}
fun computeBadge(ipo: IpoData): String {
    val now = System.currentTimeMillis()
    return when (inferIpoStatus(ipo).lowercase()) {
        "upcoming" -> {
            val isDateTba = ipo.openDate.isNullOrBlank() || ipo.openDate.equals("TBA", ignoreCase = true) || ipo.openDate == "-"
            if (isDateTba) {
                "📌 To Be Announced"
            } else {
                val openDate = parseDate(ipo.openDate)
                val diff = if (openDate != null) openDate.time - now else -1L
                val dateRange = "${ipo.openDate} → ${ipo.closeDate}"
                if (diff > 0) {
                    val days = diff / (1000 * 60 * 60 * 24)
                    if (days >= 1) "📌 $dateRange | ⏰ ${days}d mein khulega" else "📌 $dateRange | 🔥 Aaj khul raha hai!"
                } else "📌 $dateRange"
            }
        }
        "open" -> {
            val closeDate = parseDate(ipo.closeDate)
            val closeTimeEndOfDay = closeDate?.let { it.time + (24 * 60 * 60 * 1000) - 1 } ?: -1L
            val diff = closeTimeEndOfDay - now
            if (diff > 0) {
                val days = diff / (1000 * 60 * 60 * 24)
                val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
                if (days >= 1) "⏳ Closes in: ${days}d ${hours}h" else "⚠️ Sirf aaj ${hours}h baki!"
            } else {
                "✅ Subscription Closed"
            }
        }
        else -> "✅ Closed | 🚀 Listing: ${ipo.listingDate ?: "TBD"}"
    }
}
