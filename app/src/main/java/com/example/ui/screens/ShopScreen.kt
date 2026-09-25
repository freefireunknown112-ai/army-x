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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun ShopScreen(
    viewModel: GameViewModel
) {
    val catalog by viewModel.shopCatalog.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .padding(14.dp)
            .testTag("shop_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable { viewModel.navigateTo(ScreenState.MAIN_MENU) }
                        .testTag("shop_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "BLACK MARKET SUPPLY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
            }

            // Balances
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${profile?.coins ?: 2500}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${profile?.diamonds ?: 120}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dev Mode Note Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x33FFB300))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DEVELOPMENT ECONOMY: Purchases use simulated test currency only. No real money required.",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Catalog Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(catalog) { item ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .border(1.dp, if (item.isOwned) TacticalOrange.copy(alpha = 0.5f) else Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.rarity.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = when (item.rarity) {
                                "Mythic" -> Color(0xFFFF2A4B)
                                "Legendary" -> Color(0xFFFFB300)
                                else -> Color(0xFF00E5FF)
                            }
                        )
                        if (item.isOwned) {
                            Text("OWNED", fontSize = 8.sp, fontWeight = FontWeight.Black, color = TacticalOrange)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = item.description,
                        fontSize = 9.sp,
                        color = Color(0xFFAAAAAA),
                        lineHeight = 12.sp,
                        modifier = Modifier.height(28.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (item.isOwned) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TacticalOrange, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("IN ARSENAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.purchaseItem(item.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            val priceStr = if (item.priceCoins > 0) "${item.priceCoins} COINS" else "${item.priceDiamonds} GEMS"
                            Text("UNLOCK ($priceStr)", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
                        }
                    }
                }
            }
        }
    }
}
