package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TouchApp
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
import com.example.data.models.Vehicle
import com.example.data.models.WorldLoot
import com.example.ui.theme.TacticalOrange

@Composable
fun InteractionPrompt(
    loot: WorldLoot?,
    vehicle: Vehicle?,
    isDriving: Boolean,
    onLootClick: () -> Unit,
    onVehicleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = loot != null || vehicle != null || isDriving

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isDriving) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDDFF2A4B))
                        .clickable { onVehicleClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("exit_vehicle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Exit Vehicle",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EXIT VEHICLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            } else if (vehicle != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD11161B))
                        .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                        .clickable { onVehicleClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("enter_vehicle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Enter Vehicle",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "ENTER VEHICLE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB300)
                            )
                            Text(
                                text = vehicle.name,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (loot != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD11161B))
                        .border(1.5.dp, TacticalOrange, RoundedCornerShape(8.dp))
                        .clickable { onLootClick() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("pickup_loot_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Pick Up",
                            tint = TacticalOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "PICK UP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TacticalOrange
                            )
                            Text(
                                text = loot.item.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
