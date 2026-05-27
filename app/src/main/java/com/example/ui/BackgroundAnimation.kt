package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Random

class DeterministicParticle(
    val seedX: Float, // Initial normalized ratio [0..1]
    val seedY: Float, // Initial normalized ratio [0..1]
    val speedX: Float, // speed horizontal
    val speedY: Float, // speed vertical (upward)
    val size: Float,
    val opacity: Float,
    val amplitude: Float,
    val frequency: Float,
    val phase: Float
)

@Composable
fun BackgroundAnimation(variant: String) {
    // 100% pure computation, no state recomposition triggering on size dimensions!
    val particles = remember(variant) {
        val random = Random(42)
        // Reduced count due to O(N^2) line drawing operations causing ANRs on low-end devices
        val count = if (variant == "v1") 30 else 40
        List(count) {
            DeterministicParticle(
                seedX = random.nextFloat(),
                seedY = random.nextFloat(),
                speedX = if (variant == "v1") (random.nextFloat() - 0.5f) * 20f else (random.nextFloat() - 0.5f) * 35f,
                speedY = if (variant == "v1") (random.nextFloat() * 30f + 10f) else (random.nextFloat() * 50f + 20f),
                size = if (variant == "v1") random.nextFloat() * 2f + 1f else random.nextFloat() * 3.5f + 1.5f,
                opacity = if (variant == "v1") random.nextFloat() * 0.4f + 0.15f else random.nextFloat() * 0.5f + 0.15f,
                amplitude = random.nextFloat() * 50f + 20f,
                frequency = random.nextFloat() * 1.5f + 0.5f,
                phase = random.nextFloat() * (Math.PI * 2).toFloat()
            )
        }
    }

    // High performance Compose-native infinite transition replacing the manual while(true) loop
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundParticles")
    val timeSec by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeSec"
    )

    val bgBrush = remember(variant) {
        if (variant == "v1") {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0C1424),
                    Color(0xFF040814)
                )
            )
        } else {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0C4857),
                    Color(0xFF030611)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas
            
            // Pre-calculate positions for this frame to draw lines and dots efficiently
            val positions = particles.map { p ->
                val oscillationX = (Math.sin((timeSec * p.frequency + p.phase).toDouble())).toFloat() * p.amplitude
                val oscillationY = (Math.cos((timeSec * p.frequency * 0.5f + p.phase).toDouble())).toFloat() * p.amplitude * 0.5f
                
                val x = (p.seedX * width + p.speedX * timeSec + oscillationX) % width
                val y = (p.seedY * height - p.speedY * timeSec + oscillationY) % height
                
                val finalX = if (x < 0) x + width else x
                val finalY = if (y < 0) y + height else y
                
                Triple(p, finalX, finalY)
            }
            
            // Draw connecting network lines for a structured/patterned look
            val connectionDistance = if (variant == "v1") 150f else 200f
            val maxConnectionsPerParticle = 3
            
            for (i in positions.indices) {
                var connectionsCount = 0
                for (j in i + 1 until positions.size) {
                    if (connectionsCount >= maxConnectionsPerParticle) break
                    
                    val p1 = positions[i]
                    val p2 = positions[j]
                    
                    val dx = p1.second - p2.second
                    val dy = p1.third - p2.third
                    // Optimization: avoid sqrt if distSq is too big
                    val distSq = dx * dx + dy * dy
                    
                    if (distSq < connectionDistance * connectionDistance) {
                        connectionsCount++
                        val distance = Math.sqrt(distSq.toDouble()).toFloat()
                        // Alpha fades out as distance approaches max connection distance
                        val lineAlpha = (1f - (distance / connectionDistance)) * 0.35f
                        if (lineAlpha > 0f) {
                            val lineColor = if (variant == "v1") 
                                Color(138, 235, 255, (lineAlpha * 255).toInt())
                            else 
                                Color(34, 211, 238, (lineAlpha * 255).toInt())
                                
                            drawLine(
                                color = lineColor,
                                start = Offset(p1.second, p1.third),
                                end = Offset(p2.second, p2.third),
                                strokeWidth = 1f
                            )
                        }
                    }
                }
            }

            // Draw glowing dots on top
            positions.forEach { (p, finalX, finalY) ->
                if (variant == "v1") {
                    // Soft cyan-blue cyber flow glow aura
                    drawCircle(
                        color = Color(138, 235, 255, (p.opacity * 65).toInt()),
                        radius = p.size * 2.5f,
                        center = Offset(finalX, finalY)
                    )
                    // Core point
                    drawCircle(
                        color = Color(138, 235, 255, (p.opacity * 255).toInt()),
                        radius = p.size,
                        center = Offset(finalX, finalY)
                    )
                } else {
                    // Cart/Checkout style: Large floating bubbles with higher glow index
                    drawCircle(
                        color = Color(34, 211, 238, (p.opacity * 75).toInt()),
                        radius = p.size * 2.8f,
                        center = Offset(finalX, finalY)
                    )
                    drawCircle(
                        color = Color(34, 211, 238, (p.opacity * 255).toInt()),
                        radius = p.size,
                        center = Offset(finalX, finalY)
                    )
                }
            }
        }
    }
}
