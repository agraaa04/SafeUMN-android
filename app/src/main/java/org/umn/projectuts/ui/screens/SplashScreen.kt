package org.umn.projectuts.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SplashSafeColor = Color(0xFF6FA8DC)
private val SplashUmnColor = Color(0xFF1769B8)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(1800)
        currentOnFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEAF2F9), Color(0xFFD6E5F2), Color(0xFFB4CEE5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Gumpalan awan buram
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            fun cloud(cx: Float, cy: Float, radius: Float, alpha: Float) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = alpha), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }
            cloud(w * 0.20f, h * 0.06f, w * 0.65f, 0.70f)
            cloud(w * 0.75f, h * 0.14f, w * 0.60f, 0.55f)
            cloud(w * 0.30f, h * 0.30f, w * 0.55f, 0.40f)
            cloud(w * 0.85f, h * 0.38f, w * 0.50f, 0.35f)
            cloud(w * 0.15f, h * 0.70f, w * 0.55f, 0.25f)
            cloud(w * 0.80f, h * 0.88f, w * 0.60f, 0.22f)
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = SplashSafeColor, fontWeight = FontWeight.Light)) {
                    append("Safe")
                }
                withStyle(SpanStyle(color = SplashUmnColor, fontWeight = FontWeight.Normal)) {
                    append("UMN")
                }
            },
            fontSize = 56.sp,
            letterSpacing = 1.sp
        )
    }
}
