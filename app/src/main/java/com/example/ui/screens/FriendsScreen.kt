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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState

data class FriendItem(
    val id: String,
    val callSign: String,
    val level: Int,
    val status: String,
    val isOnline: Boolean
)

@Composable
fun FriendsScreen(
    viewModel: GameViewModel
) {
    var friendQuery by remember { mutableStateOf("") }
    var friendsList by remember {
        mutableStateOf(
            listOf(
                FriendItem("f_1", "GhostRecon_9", 14, "ONLINE - IN LOBBY", true),
                FriendItem("f_2", "ApexSniper", 22, "IN MATCH (SOLO BR)", true),
                FriendItem("f_3", "ValkyrieLeader", 9, "ONLINE - READY", true),
                FriendItem("f_4", "ShadowOperator", 18, "OFFLINE (2h ago)", false)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .padding(14.dp)
            .testTag("friends_screen")
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
                    .testTag("friends_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SQUAD NETWORK & OPERATIVES",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Add Friend Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = friendQuery,
                onValueChange = { friendQuery = it },
                label = { Text("Enter Operative Call-Sign / ID", fontSize = 10.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TacticalOrange,
                    unfocusedBorderColor = Color(0x33FFFFFF)
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    if (friendQuery.isNotBlank()) {
                        friendsList = friendsList + FriendItem(
                            id = "f_${System.currentTimeMillis()}",
                            callSign = friendQuery.trim(),
                            level = 1,
                            status = "ONLINE - RECRUIT",
                            isOnline = true
                        )
                        friendQuery = ""
                        viewModel.soundManager.playSound("click")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = GunmetalDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ENLIST", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Friends List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friendsList) { friend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(friend.callSign, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(TacticalOrange.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("LVL ${friend.level}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TacticalOrange)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (friend.isOnline) HealthGreen else Color(0xFF666666))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(friend.status, fontSize = 9.sp, color = if (friend.isOnline) HealthGreen else Color(0xFFAAAAAA))
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.soundManager.playSound("click") },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, tint = GunmetalDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("INVITE SQUAD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GunmetalDark)
                    }
                }
            }
        }
    }
}
