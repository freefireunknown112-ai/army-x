package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun ProfileScreen(
    viewModel: GameViewModel
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()
    val matchHistory by viewModel.matchHistory.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .padding(14.dp)
            .testTag("profile_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FFFFFF))
                    .clickable { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                    .testTag("profile_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "OPERATIVE DOSSIER",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Profile Top Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(TacticalOrange.copy(alpha = 0.2f))
                    .border(2.dp, TacticalOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TacticalOrange, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile?.username ?: "Operative",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "ID: ${profile?.playerId ?: "AX-UNKNOWN"}",
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "LEVEL ${profile?.level ?: 1} - ${profile?.currentXp ?: 0}/${profile?.xpForNextLevel ?: 1000} XP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TacticalOrange
                )
            }

            // Stats Quick Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DossierStat("KILLS", "${profile?.totalKills ?: 0}")
                DossierStat("WINS", "${profile?.totalWins ?: 0}")
                DossierStat("MATCHES", "${profile?.totalMatches ?: 0}")
                DossierStat("K/D", String.format("%.2f", profile?.kdRatio ?: 0f))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Selector: Achievements vs Match History
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = TacticalOrange
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ACHIEVEMENTS (${achievements.count { it.isCompleted }}/${achievements.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("MATCH LOGS (${matchHistory.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(achievements) { ach ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, if (ach.isCompleted) TacticalOrange.copy(alpha = 0.5f) else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (ach.isCompleted) TacticalOrange else Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ach.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = ach.description, fontSize = 10.sp, color = Color(0xFFAAAAAA))
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (ach.currentProgress.toFloat() / ach.targetProgress).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = TacticalOrange,
                                trackColor = Color(0x33FFFFFF)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        if (ach.isClaimed) {
                            Text("CLAIMED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF888888))
                        } else if (ach.isCompleted) {
                            Button(
                                onClick = { viewModel.claimAchievement(ach.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("CLAIM", fontSize = 10.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
                            }
                        } else {
                            Text("${ach.currentProgress}/${ach.targetProgress}", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                        }
                    }
                }
            }
        } else {
            if (matchHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No recorded battle history yet. Drop into Battle Royale!", fontSize = 12.sp, color = Color(0xFF888888))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matchHistory) { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, if (m.isVictory) TacticalOrange else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (m.isVictory) "VICTORY ROYALE #1" else "PLACEMENT #${m.placement} / ${m.totalPlayers}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (m.isVictory) TacticalOrange else Color.White
                                )
                                Text(text = m.mode, fontSize = 9.sp, color = Color(0xFFAAAAAA))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("KILLS: ${m.kills}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("+${m.xpEarned} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                Text("+${m.coinsEarned} C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DossierStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}
