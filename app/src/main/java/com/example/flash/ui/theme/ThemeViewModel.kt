package com.example.flash.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flash.data.preferences.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val prefs: ThemePreferences) : ViewModel() {

    val themeMode = prefs.themeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeMode.SYSTEM
    )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            prefs.saveTheme(mode)
        }
    }
}
