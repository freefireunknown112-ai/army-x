package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Weapon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.TacticalOrange

@Composable
fun WeaponCard(
    weapons: List<Weapon>,
    currentWeaponIndex: Int,
    onSwitchWeapon: (Int) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeWeapon = if (weapons.isNotEmpty() && currentWeaponIndex in weapons.indices) {
        weapons[currentWeaponIndex]
    } else null

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCC11151A))
            .border(1.5.dp, TacticalOrange.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("weapon_hud_card"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Weapon Slot Selectors
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            weapons.forEachIndexed { index, w ->
                val isSelected = index == currentWeaponIndex
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) TacticalOrange else Color(0x33FFFFFF))
                        .clickable { onSwitchWeapon(index) }
                        .testTag("weapon_slot_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SLOT ${index + 1}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        // Active Weapon Details
        if (activeWeapon != null) {
            Column(modifier = Modifier.width(90.dp)) {
                Text(
                    text = activeWeapon.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = activeWeapon.type.displayName,
                    fontSize = 9.sp,
                    color = Color(0xFFAAAAAA),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${activeWeapon.currentAmmo}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (activeWeapon.currentAmmo <= 5) DangerRed else TacticalOrange
                    )
                    Text(
                        text = " / ${activeWeapon.reserveAmmo}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDDDDDD),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }

        // Reload Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TacticalOrange.copy(alpha = 0.25f))
                .border(1.dp, TacticalOrange, RoundedCornerShape(8.dp))
                .clickable { onReload() }
                .testTag("reload_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload",
                tint = TacticalOrange,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
