package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.TeleStreamApp
import com.example.ui.theme.TeleStreamTheme
import com.example.ui.viewmodel.TeleStreamViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TeleStreamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("TeleStreamCrash", "Uncaught exception in thread ${thread.name}", throwable)
        }

        enableEdgeToEdge()
        setContent {
            TeleStreamTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TeleStreamApp(viewModel = viewModel)
                }
            }
        }
    }
}

