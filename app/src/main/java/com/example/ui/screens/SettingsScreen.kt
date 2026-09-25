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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.database.entities.UserSettingsEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun SettingsScreen(
    viewModel: GameViewModel
) {
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()

    var cameraSens by remember(currentSettings) { mutableFloatStateOf(currentSettings.cameraSensitivity) }
    var aimSens by remember(currentSettings) { mutableFloatStateOf(currentSettings.aimSensitivity) }
    var soundVol by remember(currentSettings) { mutableFloatStateOf(currentSettings.soundVolume) }
    var musicVol by remember(currentSettings) { mutableFloatStateOf(currentSettings.musicVolume) }
    var graphicsQuality by remember(currentSettings) { mutableStateOf(currentSettings.graphicsQuality) }
    var shadows by remember(currentSettings) { mutableStateOf(currentSettings.shadowsEnabled) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .padding(14.dp)
            .testTag("settings_screen")
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
                    .testTag("settings_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SYSTEM & COMBAT SETTINGS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // GRAPHICS CONFIGURATION
            SettingsSection(title = "GRAPHICS & DISPLAY") {
                Text("Quality Preset", fontSize = 11.sp, color = Color(0xFFAAAAAA))
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LOW", "MEDIUM", "HIGH").forEach { preset ->
                        val isSelected = graphicsQuality == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) TacticalOrange else Color(0x33FFFFFF))
                                .clickable {
                                    graphicsQuality = preset
                                    viewModel.updateSettings(
                                        currentSettings.copy(graphicsQuality = preset)
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GunmetalDark else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dynamic Shadows & Lighting", fontSize = 12.sp, color = Color.White)
                    Switch(
                        checked = shadows,
                        onCheckedChange = {
                            shadows = it
                            viewModel.updateSettings(currentSettings.copy(shadowsEnabled = it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TacticalOrange,
                            checkedTrackColor = TacticalOrange.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // CONTROLS & SENSITIVITY
            SettingsSection(title = "TACTICAL TOUCH SENSITIVITY") {
                Text("Look / Camera Sensitivity: ${(cameraSens * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                Slider(
                    value = cameraSens,
                    onValueChange = {
                        cameraSens = it
                        viewModel.updateSettings(currentSettings.copy(cameraSensitivity = it))
                    },
                    valueRange = 0.2f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = TacticalOrange, activeTrackColor = TacticalOrange)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("Aim Down Sights (ADS) Sensitivity: ${(aimSens * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                Slider(
                    value = aimSens,
                    onValueChange = {
                        aimSens = it
                        viewModel.updateSettings(currentSettings.copy(aimSensitivity = it))
                    },
                    valueRange = 0.2f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = TacticalOrange, activeTrackColor = TacticalOrange)
                )
            }

            // AUDIO CONTROLS
            SettingsSection(title = "AUDIO & SOUND EFFECTS") {
                Text("Sound Effects / Gunfire: ${(soundVol * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                Slider(
                    value = soundVol,
                    onValueChange = {
                        soundVol = it
                        viewModel.updateSettings(currentSettings.copy(soundVolume = it))
                    },
                    valueRange = 0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = TacticalOrange, activeTrackColor = TacticalOrange)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text("Tactical Atmosphere Music: ${(musicVol * 100).toInt()}%", fontSize = 11.sp, color = Color.White)
                Slider(
                    value = musicVol,
                    onValueChange = {
                        musicVol = it
                        viewModel.updateSettings(currentSettings.copy(musicVolume = it))
                    },
                    valueRange = 0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = TacticalOrange, activeTrackColor = TacticalOrange)
                )
            }

            // ACCOUNT & LOGOUT
            SettingsSection(title = "SESSION & CREDENTIALS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Operative Session", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Persistent Local Development Profile", fontSize = 9.sp, color = Color(0xFFAAAAAA))
                    }
                    Button(
                        onClick = { viewModel.navigateTo(ScreenState.AUTH) },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp).testTag("logout_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SWITCH ACCOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            color = TacticalOrange
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}
