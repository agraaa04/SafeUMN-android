package org.umn.projectuts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.umn.projectuts.navigation.SafeUmnApp
import org.umn.projectuts.ui.theme.SafeUMNTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeUMNTheme {
                SafeUmnApp()
            }
        }
    }
}
