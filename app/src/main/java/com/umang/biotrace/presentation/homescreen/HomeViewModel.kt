package com.umang.biotrace.presentation.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umang.biotrace.data.LastScanStore
import com.umang.biotrace.data.LastScanSummary
import com.umang.biotrace.data.repository.remote.ApiResult
import com.umang.biotrace.data.repository.remote.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val lastScanStore: LastScanStore,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Load whatever was saved locally on startup
        loadLocalLastScan()
    }

    private fun loadLocalLastScan() {
        val saved = lastScanStore.load()
        _uiState.update { it.copy(lastScan = saved) }
    }

    // Called from HomeScreen when it becomes visible — refreshes from server
    fun refreshFromServer() {
        viewModelScope.launch {
            when (val result = scanRepository.getAllScans()) {
                is ApiResult.Success -> {
                    // The last item in the list is the most recently uploaded scan
                    val latest = result.data.lastOrNull()
                    if (latest != null) {
                        lastScanStore.save(latest)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                lastScan = lastScanStore.load()
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                }
            }
        }
    }
}

data class HomeUiState(
    val lastScan: LastScanSummary? = null,
    val isLoading: Boolean = false
)