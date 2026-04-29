package com.IPO.Tracker.model

data class IpoData(
    val id: String,
    val name: String,
    val gmp: String,
    val status: String,
    val openDate: String?,
    val closeDate: String?,
    val priceBand: String?,
    val subscription: String,
    val allotment_prob: String,
    val hype_meter: String,
    val red_flags: List<String>
)
