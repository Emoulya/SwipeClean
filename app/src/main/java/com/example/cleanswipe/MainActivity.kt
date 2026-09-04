package com.example.cleanswipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cleanswipe.ui.CleanSwipeApp
import com.example.cleanswipe.ui.theme.CleanSwipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanSwipeTheme {
                CleanSwipeApp()
            }
        }
    }
}