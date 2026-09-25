package com.example.ui.screens

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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun ResultScreen(
    viewModel: GameViewModel
) {
    val result = viewModel.lastMatchResult.collectAsStateWithLifecycle().value
    val isVictory = result?.first ?: false
    val kills = result?.second ?: 0
    val placement = result?.third ?: 1

    val title = if (isVictory) "VICTORY ROYALE" else "ELIMINATED"
    val subtitle = if (isVictory) "CHAMPION OF ARMY X" else "BETTER LUCK NEXT DROP"
    val accentColor = if (isVictory) TacticalOrange else DangerRed

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .testTag("result_screen"),
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
                .background(Color(0xF00A0E12))
        )

        Column(
            modifier = Modifier
                .width(480.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface.copy(alpha = 0.96f))
                .border(2.dp, accentColor, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.Shield,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = accentColor
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFFAAAAAA)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Stats grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33FFFFFF))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("RANK", "#$placement / 20", accentColor)
                StatItem("KILLS", "$kills", Color.White)
                StatItem("XP GAINED", "+${if (isVictory) 500 + kills * 50 else 150 + kills * 40}", Color(0xFF00E5FF))
                StatItem("COINS", "+${if (isVictory) 800 + kills * 70 else 250 + kills * 50}", Color(0xFFFFB300))
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenState.MAIN_MENU) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("result_main_menu_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Menu", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MAIN MENU", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.restartMatch() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("play_again_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Play Again", tint = GunmetalDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PLAY AGAIN", fontSize = 12.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
    }
}
