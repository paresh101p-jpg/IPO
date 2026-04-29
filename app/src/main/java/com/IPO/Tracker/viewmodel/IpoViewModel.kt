package com.IPO.Tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.IPO.Tracker.model.IpoData
import com.IPO.Tracker.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class IpoUiState {
    object Loading : IpoUiState()
    data class Success(val ipos: List<IpoData>) : IpoUiState()
    data class Error(val message: String) : IpoUiState()
}

class IpoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<IpoUiState>(IpoUiState.Loading)
    val uiState: StateFlow<IpoUiState> = _uiState

    private val _buybacks = MutableStateFlow<List<com.IPO.Tracker.model.BuybackData>>(emptyList())
    val buybacks: StateFlow<List<com.IPO.Tracker.model.BuybackData>> = _buybacks

    private val _news = MutableStateFlow<List<com.IPO.Tracker.model.NewsData>>(emptyList())
    val news: StateFlow<List<com.IPO.Tracker.model.NewsData>> = _news

    private val apiService = ApiService.create()

    init {
        fetchAllData()
    }

    fun fetchAllData() {
        viewModelScope.launch {
            _uiState.value = IpoUiState.Loading
            try {
                // Fetch IPOs (Primary)
                try {
                    val timestamp = System.currentTimeMillis()
                    val ipos = apiService.getIpos(timestamp)
                    _uiState.value = IpoUiState.Success(ipos)
                } catch (e: Exception) {
                    _uiState.value = IpoUiState.Error("IPO Data Error: ${e.message}")
                }

                // Fetch Buybacks (Secondary)
                try {
                    _buybacks.value = apiService.getBuybacks(System.currentTimeMillis())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Fetch News (Secondary)
                try {
                    _news.value = apiService.getNews(System.currentTimeMillis())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
            } catch (e: Exception) {
                _uiState.value = IpoUiState.Error("Connection Error: ${e.message}")
            }
        }
    }
}
