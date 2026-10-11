package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val chatRepository: ChatRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("nexa_ai_settings", Context.MODE_PRIVATE)

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _defaultModel = MutableStateFlow(prefs.getString("default_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val defaultModel: StateFlow<String> = _defaultModel.asStateFlow()

    private val _streamingEnabled = MutableStateFlow(prefs.getBoolean("streaming_enabled", true))
    val streamingEnabled: StateFlow<Boolean> = _streamingEnabled.asStateFlow()

    private val _temperature = MutableStateFlow(prefs.getFloat("temperature", 0.7f))
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "dark") ?: "dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setCustomApiKey(key: String) {
        val clean = key.trim()
        _customApiKey.value = clean
        prefs.edit().putString("custom_api_key", clean).apply()
        _statusMessage.value = "API configuration saved"
    }

    fun setDefaultModel(model: String) {
        _defaultModel.value = model
        prefs.edit().putString("default_model", model).apply()
    }

    fun setStreamingEnabled(enabled: Boolean) {
        _streamingEnabled.value = enabled
        prefs.edit().putBoolean("streaming_enabled", enabled).apply()
    }

    fun setTemperature(temp: Float) {
        _temperature.value = temp
        prefs.edit().putFloat("temperature", temp).apply()
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            chatRepository.deleteAllConversations()
            _statusMessage.value = "All conversations cleared"
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
