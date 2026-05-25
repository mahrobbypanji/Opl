package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun BackgroundAnimation(variant: String) {
    // High-performance static dynamic brush selection that maps perfectly to the user's aesthetics
    val bgBrush = remember(variant) {
        if (variant == "v1") {
            // v1: Deep, eye-safe Cyber Slate Theme vertical gradient
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0C1424),
                    Color(0xFF040814)
                )
            )
        } else {
            // v2: Luxury Mesh Gradient theme represented by a rich radial blend
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0E5466),
                    Color(0xFF030712)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    )
}
