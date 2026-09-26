package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val scale = remember { Animatable(0.92f) }
    val alpha = remember { Animatable(0f) }
    var statusText by remember { mutableStateOf("Inicializando sistema...") }

    LaunchedEffect(Unit) {
        // Fade in and scale animation
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        // Progress steps simulation
        statusText = "Carregando configurações..."
        progress.animateTo(
            targetValue = 0.35f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        )
        delay(150)

        statusText = "Calibrando módulo GPS e fotos..."
        progress.animateTo(
            targetValue = 0.75f,
            animationSpec = tween(durationMillis = 700, easing = LinearEasing)
        )
        delay(150)

        statusText = "Pronto!"
        progress.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 450, easing = LinearEasing)
        )
        delay(300)
        onTimeout()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val screenHeight = maxHeight
        val isSmallScreen = screenHeight < 640.dp
        val logoMaxHeight = if (isSmallScreen) (screenHeight * 0.32f).coerceAtLeast(140.dp) else 240.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = if (isSmallScreen) 12.dp else 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 24.dp))

            // Central Card with the provided Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_splash_logo),
                    contentDescription = "Registro Fotográfico de Campo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .widthIn(max = 420.dp)
                        .height(logoMaxHeight)
                )
            }

            Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 32.dp))

            // Loading Section (Progress bar and status label)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .widthIn(max = 340.dp)
                    .alpha(alpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Modern Progress Bar
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF0284C7), // Vibrant cyan/blue
                    trackColor = Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round
                )

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontSize = if (isSmallScreen) 13.sp else 14.sp
                )
            }

            Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 28.dp))

            Text(
                text = "Versão 1.0 • Modo Campo Offline",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        }
    }
}
