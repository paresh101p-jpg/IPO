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
    return text
        ?.let { Regex("\\d+").findAll(it).mapNotNull { match -> match.value.replace(",", "").toIntOrNull() }.toList() }
        .orEmpty()
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
