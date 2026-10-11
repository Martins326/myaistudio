package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.GeminiApiService
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolsViewModel(private val geminiService: GeminiApiService) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toolOutput = MutableStateFlow<String?>(null)
    val toolOutput: StateFlow<String?> = _toolOutput.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun runTool(
        toolName: String,
        input: String,
        model: String = "gemini-3.5-flash",
        customApiKey: String? = null
    ) {
        if (input.isBlank() || _isLoading.value) return

        _isLoading.value = true
        _error.value = null
        _toolOutput.value = null

        val prompt = when (toolName) {
            "code_review" -> """
                You are a Principal Software Architect reviewing code for NEXA-AI.
                Analyze the following code for bugs, security vulnerabilities, performance bottlenecks, and style improvements:
                
                $input
                
                Provide:
                1. Executive Quality Score (1-10)
                2. Key Issues & Bugs Found
                3. Refactored, Production-Grade Code (with markdown language tag)
                4. Complexity analysis (Big-O time and space)
            """.trimIndent()

            "code_test" -> """
                Generate exhaustive automated unit test cases for the following code snippet.
                Include edge cases, null inputs, and error conditions:
                
                $input
            """.trimIndent()

            "writing_polish" -> """
                You are an expert editor at NEXA-AI.
                Rewrite and elevate the following text for clarity, professional impact, and elegance while preserving original intent:
                
                $input
            """.trimIndent()

            "writing_email" -> """
                Draft a professional, compelling, and polished email based on this outline/prompt:
                
                $input
            """.trimIndent()

            "math_solve" -> """
                Solve this mathematical or scientific problem step-by-step with rigorous logic and formula breakdown:
                
                $input
            """.trimIndent()

            "data_csv" -> """
                Convert the following data description or unstructured text into a clean, well-formatted CSV and markdown table with appropriate columns:
                
                $input
            """.trimIndent()

            else -> input
        }

        viewModelScope.launch {
            val res = geminiService.generateContent(
                model = model,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                ),
                customKey = customApiKey
            )

            _isLoading.value = false
            res.fold(
                onSuccess = { (text, _) ->
                    _toolOutput.value = text
                },
                onFailure = { ex ->
                    _error.value = ex.message ?: "Tool execution failed."
                }
            )
        }
    }

    fun clearOutput() {
        _toolOutput.value = null
        _error.value = null
    }
}
