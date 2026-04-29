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

    private val apiService = ApiService.create()

    init {
        fetchIpos()
    }

    fun fetchIpos() {
        viewModelScope.launch {
            _uiState.value = IpoUiState.Loading
            try {
                val response = apiService.getIpos()
                _uiState.value = IpoUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = IpoUiState.Error("Error fetching data: ${e.message}")
            }
        }
    }
}
