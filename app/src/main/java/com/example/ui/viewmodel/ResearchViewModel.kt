package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ResearchRepository
import com.example.data.repository.ResearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResearchViewModel(private val repository: ResearchRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _totalSteps = MutableStateFlow(4)
    val totalSteps: StateFlow<Int> = _totalSteps.asStateFlow()

    private val _progressMessage = MutableStateFlow("")
    val progressMessage: StateFlow<String> = _progressMessage.asStateFlow()

    private val _researchResult = MutableStateFlow<ResearchResult?>(null)
    val researchResult: StateFlow<ResearchResult?> = _researchResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun startDeepResearch(topic: String, model: String = "gemini-3.5-flash", customApiKey: String? = null) {
        if (topic.isBlank() || _isLoading.value) return

        _isLoading.value = true
        _error.value = null
        _researchResult.value = null
        _currentStep.value = 1
        _progressMessage.value = "Initializing multi-step research workflow..."

        viewModelScope.launch {
            val result = repository.executeDeepResearch(
                topic = topic,
                model = model,
                customApiKey = customApiKey,
                onProgress = { step, total, message ->
                    _currentStep.value = step
                    _totalSteps.value = total
                    _progressMessage.value = message
                }
            )

            _isLoading.value = false
            result.fold(
                onSuccess = { res ->
                    _researchResult.value = res
                },
                onFailure = { ex ->
                    _error.value = ex.message ?: "Deep research failed."
                }
            )
        }
    }

    fun clearResult() {
        _researchResult.value = null
        _error.value = null
    }
}
