package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SophisticatedBackground
import com.example.viewmodel.GameViewModel
import com.example.data.AndroidGameScoreRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Android Game Score Repository
        val repository = AndroidGameScoreRepository(applicationContext)
        val viewModel = GameViewModel(repository)

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("arcade_scaffold"),
                    containerColor = SophisticatedBackground
                ) { innerPadding ->
                    ArcadeApp(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
