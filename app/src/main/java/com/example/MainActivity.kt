package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import com.example.ui.StockPulseApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: InventoryViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val systemDark = isSystemInDarkTheme()
      val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
      }

      MyApplicationTheme(darkTheme = isDark) {
        Surface(modifier = Modifier.fillMaxSize()) {
          StockPulseApp(
            viewModel = viewModel,
            isDarkTheme = isDark
          )
        }
      }
    }
  }
}

