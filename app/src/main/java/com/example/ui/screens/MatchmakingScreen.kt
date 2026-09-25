package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState
import kotlinx.coroutines.delay

@Composable
fun MatchmakingScreen(
    viewModel: GameViewModel
) {
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    var playerCount by remember { mutableIntStateOf(1) }
    var elapsedSec by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (playerCount < selectedMode.maxPlayers) {
            delay(350)
            playerCount += (1..3).random()
            if (playerCount > selectedMode.maxPlayers) {
                playerCount = selectedMode.maxPlayers
            }
        }
        delay(600)
        viewModel.launchGameSession()
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSec++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .testTag("matchmaking_screen"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.armyx_banner_1790331672910),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE0A0E12))
        )

        Column(
            modifier = Modifier
                .width(460.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface.copy(alpha = 0.95f))
                .border(1.5.dp, TacticalOrange.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = TacticalOrange,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "SEARCHING OPERATIVES...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Map and Mode Details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33FFFFFF))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = TacticalOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAP: OUTPOST ZERO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = selectedMode.title,
                        fontSize = 10.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$playerCount / ${selectedMode.maxPlayers}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TacticalOrange
                    )
                    Text(
                        text = "TIME: ${elapsedSec}s",
                        fontSize = 10.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { (playerCount.toFloat() / selectedMode.maxPlayers).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TacticalOrange,
                trackColor = Color(0x33FFFFFF)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("cancel_matchmaking_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.launchGameSession() },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("force_deploy_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange)
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = "Start", tint = GunmetalDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("INSTANT DROP", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
                }
            }
        }
    }
}
