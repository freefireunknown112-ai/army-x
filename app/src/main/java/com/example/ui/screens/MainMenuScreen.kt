package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.GameMode
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.ui.theme.TacticalOrangeBright
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .testTag("main_menu_screen")
    ) {
        // Hero Background
        Image(
            painter = painterResource(id = R.drawable.armyx_banner_1790331672910),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Shade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xDD0A0E12),
                            Color(0xAA0A0E12),
                            Color(0xF00A0E12)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface.copy(alpha = 0.9f))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Player Profile Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(ScreenState.PROFILE) }
                        .testTag("profile_badge_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TacticalOrange.copy(alpha = 0.2f))
                            .border(1.5.dp, TacticalOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = TacticalOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.username ?: "Operative",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(TacticalOrange)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "LVL ${profile?.level ?: 1}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GunmetalDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        val currentXp = profile?.currentXp ?: 0
                        val nextXp = profile?.xpForNextLevel ?: 1000
                        val progress = (currentXp.toFloat() / nextXp).coerceIn(0f, 1f)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = TacticalOrange,
                                trackColor = Color(0x33FFFFFF)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$currentXp/$nextXp XP",
                                fontSize = 8.sp,
                                color = Color(0xFFAAAAAA)
                            )
                        }
                    }
                }

                // Center: Army X Branding
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ARMY ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                    Text(
                        text = "X",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TacticalOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x33FFB300))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DEV ONLINE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }

                // Right: Currencies & Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Coins
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile?.coins ?: 2500}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Diamonds
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "Diamonds",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile?.diamonds ?: 120}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Settings Icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable { viewModel.navigateTo(ScreenState.SETTINGS) }
                            .testTag("menu_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // CENTER-BOTTOM: Game Mode Selector & Main Play Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Game Mode Cards
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SELECT MISSION PROTOCOL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFAAAAAA)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GameMode.values().forEach { mode ->
                            val isSelected = mode == selectedMode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) TacticalOrange.copy(alpha = 0.25f) else Color(0x9911151A))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) TacticalOrange else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectGameMode(mode) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("mode_${mode.name}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column {
                                    Text(
                                        text = mode.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TacticalOrange else Color.White
                                    )
                                    Text(
                                        text = "${mode.maxPlayers} Operatives",
                                        fontSize = 9.sp,
                                        color = Color(0xFF888888)
                                    )
                                }
                            }
                        }
                    }
                }

                // Big Prominent "PLAY BATTLE ROYALE" Button
                Button(
                    onClick = { viewModel.startMatchmaking() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalOrange,
                        contentColor = GunmetalDark
                    ),
                    modifier = Modifier
                        .height(58.dp)
                        .width(220.dp)
                        .testTag("main_play_button"),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DEPLOY NOW",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "BATTLE ROYALE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GunmetalDark.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTTOM NAVIGATION BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface.copy(alpha = 0.95f))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem("PROFILE", Icons.Default.Person, false) { viewModel.navigateTo(ScreenState.PROFILE) }
                BottomNavItem("ARMORY", Icons.Default.Shield, false) { viewModel.navigateTo(ScreenState.ARMORY) }
                BottomNavItem("SHOP", Icons.Default.ShoppingBag, false) { viewModel.navigateTo(ScreenState.SHOP) }
                BottomNavItem("SQUAD / FRIENDS", Icons.Default.People, false) { viewModel.navigateTo(ScreenState.FRIENDS) }
                BottomNavItem("SETTINGS", Icons.Default.Settings, false) { viewModel.navigateTo(ScreenState.SETTINGS) }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TacticalOrange else Color(0xFFAAAAAA),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TacticalOrange else Color.White
        )
    }
}
