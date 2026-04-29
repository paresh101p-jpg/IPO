package com.IPO.Tracker.model

data class IpoData(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val gmp: String,
    val gmpTrend: List<Float> = emptyList(), // For graph
    val status: String, // Open, Closed, Upcoming, Listed
    val openDate: String?,
    val closeDate: String?,
    val listingDate: String? = null,
    val offerPrice: String? = null,
    val issueSize: String? = null,
    val lotSize: String? = null,
    val retailLotsAllowed: String? = null,
    val expectedPremium: String? = null,
    @com.google.gson.annotations.SerializedName("subscription")
    val subscriptionText: String? = null,
    val subscriptionDetails: SubscriptionData? = null,
    val allotment_prob: String,
    val hype_meter: String,
    val red_flags: List<String> = emptyList(),
    val financials: CompanyFinancials? = null,
    val categoryReservation: String? = null,
    val aboutCompany: String? = null,
    val promoters: String? = null,
    val issueObjective: String? = null,
    val anchorInvestors: String? = null,
    val contactDetails: String? = null,
    val registrarDetails: String? = null,
    val listingPrice: String? = null,
    val currentPrice: String? = null,
    val fiftyTwoWeekHigh: String? = null,
    val fiftyTwoWeekLow: String? = null,
    val valuations: String? = null,
    val allotmentLink: String? = null,
    val averageRating: Float = 0f,
    val totalRatingsCount: Int = 0,
    val whaleAlert: String? = null,
    val peerComparison: List<PeerData> = emptyList()
)

data class PeerData(
    val companyName: String,
    val peRatio: String,
    val marketCap: String,
    val cmp: String
)

data class SubscriptionData(
    val day1: String,
    val day2: String,
    val day3: String,
    val retailTotal: String,
    val totalApplications: String
)

data class CompanyFinancials(
    val revenue: String,
    val profit: String,
    val debt: String
)

data class BuybackData(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val status: String, // Current, Upcoming, Closed
    val buybackPrice: String,
    val recordDate: String,
    val openDate: String,
    val closeDate: String,
    val issueSizeShares: String,
    val issueSizeAmount: String,
    val buybackRatio: String,
    val aboutCompany: String,
    val howToParticipate: String,
    val investmentCalculation: String,
    val docsLink: String? = null
)

data class NewsData(
    val id: String,
    val headline: String,
    val summary: String,
    val imageUrl: String,
    val date: String
)

data class DematAccount(
    val id: String,
    val name: String,
    val panNumber: String,
    val dpId: String,
    val clientId: String,
    val upiId: String
)
