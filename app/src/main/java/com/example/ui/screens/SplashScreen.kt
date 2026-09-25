package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(700))
        progressAnim.animateTo(1f, animationSpec = tween(1400))
        delay(300)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Hero Background Art
        Image(
            painter = painterResource(id = R.drawable.armyx_banner_1790331672910),
            contentDescription = "Army X Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark Tactical Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x990A0E12),
                            Color(0xE60A0E12),
                            Color(0xFA0A0E12)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .padding(24.dp)
        ) {
            // Tactical Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x8814191E))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground_1790331656924),
                    contentDescription = "Army X Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ARMY X",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                color = TacticalOrange,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = "3D MOBILE BATTLE ROYALE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = Color(0xFFDDDDDD)
            )

            Spacer(modifier = Modifier.height(30.dp))

            LinearProgressIndicator(
                progress = { progressAnim.value },
                modifier = Modifier
                    .width(220.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = TacticalOrange,
                trackColor = Color(0x44FFFFFF)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "INITIALIZING COMBAT PROTOCOLS...",
                fontSize = 9.sp,
                color = Color(0xFF8E9BA8),
                letterSpacing = 1.sp
            )
        }
    }
}
