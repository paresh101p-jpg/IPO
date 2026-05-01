package com.IPO.Tracker.network

import com.IPO.Tracker.model.IpoData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("backend/ipos.json")
    suspend fun getIpos(@Query("t") timestamp: Long = System.currentTimeMillis()): List<IpoData>

    @GET("backend/buybacks.json")
    suspend fun getBuybacks(@Query("t") timestamp: Long = System.currentTimeMillis()): List<com.IPO.Tracker.model.BuybackData>

    @GET("backend/news.json")
    suspend fun getNews(@Query("t") timestamp: Long = System.currentTimeMillis()): List<com.IPO.Tracker.model.NewsData>

    companion object {
        // Production API url from GitHub Raw
        private const val BASE_URL = "https://raw.githubusercontent.com/paresh101p-jpg/IPO/main/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
