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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.models.Weapon
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

@Composable
fun ArmoryScreen(
    viewModel: GameViewModel
) {
    val weapons = remember { Weapon.getAllAvailableWeapons() }
    var selectedWeapon by remember { mutableStateOf(weapons[1]) } // Default AR-X

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .padding(14.dp)
            .testTag("armory_screen")
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
                    .testTag("armory_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "MILITARY WEAPONS ARMORY",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Left: Weapon List
            LazyColumn(
                modifier = Modifier.width(220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(weapons) { w ->
                    val isSelected = w.id == selectedWeapon.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TacticalOrange.copy(alpha = 0.2f) else DarkSurface)
                            .border(1.dp, if (isSelected) TacticalOrange else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                            .clickable {
                                selectedWeapon = w
                                viewModel.soundManager.playSound("click")
                            }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = w.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TacticalOrange else Color.White
                            )
                            Text(
                                text = w.type.displayName,
                                fontSize = 9.sp,
                                color = Color(0xFFAAAAAA)
                            )
                        }
                    }
                }
            }

            // Right: Detailed Weapon Blueprint & Stats
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedWeapon.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TacticalOrange
                        )
                        Text(
                            text = "CALIBER: ${selectedWeapon.ammoType.label}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = { viewModel.soundManager.playSound("click") },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = GunmetalDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EQUIP LOADOUT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedWeapon.description,
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Progress Bars
                WeaponStatBar("DAMAGE PER SHOT", selectedWeapon.damage / 120f, "${selectedWeapon.damage.toInt()} HP")
                WeaponStatBar("FIRE RATE", (1000f / selectedWeapon.fireRateMs) / 15f, "${(1000f / selectedWeapon.fireRateMs).toInt()} RPM")
                WeaponStatBar("EFFECTIVE RANGE", selectedWeapon.range / 250f, "${selectedWeapon.range.toInt()} METERS")
                WeaponStatBar("ACCURACY", selectedWeapon.accuracy, "${(selectedWeapon.accuracy * 100).toInt()}%")
                WeaponStatBar("MAGAZINE CAPACITY", selectedWeapon.magazineSize / 100f, "${selectedWeapon.magazineSize} ROUNDS")
                WeaponStatBar("BULLET VELOCITY", selectedWeapon.bulletSpeed / 300f, "${selectedWeapon.bulletSpeed.toInt()} M/S")
            }
        }
    }
}

@Composable
private fun WeaponStatBar(label: String, progress: Float, valueStr: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFAAAAAA))
            Text(text = valueStr, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(2.dp)),
            color = TacticalOrange,
            trackColor = Color(0x33FFFFFF)
        )
    }
}
