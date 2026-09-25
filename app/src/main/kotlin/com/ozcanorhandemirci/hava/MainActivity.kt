package com.ozcanorhandemirci.hava

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.ozcanorhandemirci.hava.core.designsystem.theme.HavaTheme

/**
 * Single entry point of the application.
 *
 * The window is drawn edge to edge because the sky is the background of the
 * whole screen, including the area behind the system bars.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HavaTheme {
                HavaApp()
            }
        }
    }
}
