package com.example.inplan.ui.screens


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.inplan.R

/**
 * Brand blue sampled from the InPlan app icon (#013FC9).
 * If you already have a brand color defined in ui.theme (e.g. BrandBlue),
 * swap this out for that so there's a single source of truth.
 */
private val InPlanBlue = Color(0xFF013FC9)

/**
 * Full-bleed splash shown while MainActivity resolves the start destination
 * (auth check / profile check / deep link). Drop this in place of the old
 * SplashLoading() composable.
 *
 * Requires the logo PNG saved as res/drawable/ic_inplan_logo.png
 * (transparent background, logo mark only works too — see notes below).
 */
@Composable
fun SplashScreen() {
    val logoScale = remember { Animatable(0.85f) }
    val logoAlpha = remember { Animatable(0f) }
    var taglineVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(durationMillis = 450, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(durationMillis = 550, easing = EaseOutCubic))
    }
    LaunchedEffect(Unit) {
        // tagline follows the logo in
        kotlinx.coroutines.delay(250)
        taglineVisible = true
    }
    val taglineAlpha by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InPlanBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.inplanlogo),
                contentDescription = "InPlan",
                modifier = Modifier
                    .size(240.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(Modifier.height(20.dp))

            AnimatedTagline(visible = taglineVisible)
        }
    }
}

@Composable
private fun AnimatedTagline(visible: Boolean) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            alpha.animateTo(1f, tween(durationMillis = 400, easing = EaseOutCubic))
        }
    }
    Text(
        text = "Plan · Split · Settle",
        color = Color.White.copy(alpha = 0.88f),
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 2.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.alpha(alpha.value)
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}