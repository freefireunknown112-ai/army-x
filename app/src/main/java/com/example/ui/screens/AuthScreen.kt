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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel

@Composable
fun AuthScreen(
    viewModel: GameViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Guest, 1: Email Login, 2: Register
    var emailInput by remember { mutableStateOf("operative@armyx.mil") }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var usernameInput by remember { mutableStateOf("DeltaOperative") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GunmetalDark)
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.armyx_banner_1790331672910),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xDD0A0E12),
                            Color(0xF50A0E12)
                        )
                    )
                )
        )

        // Auth Card
        Column(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface.copy(alpha = 0.95f))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Development Mode Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x33FFB300))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Dev Mode",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DEVELOPMENT MODE (LOCAL AUTH)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB300)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "OPERATIVE ACCESS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0x22FFFFFF),
                contentColor = TacticalOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GUEST", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("LOGIN", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("REGISTER", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Guest Login
                    Text(
                        text = "Instant deployment with persistent local operative credentials.",
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA),
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.loginGuest() },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("guest_login_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "DEPLOY AS GUEST",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = GunmetalDark
                        )
                    }
                }

                1 -> {
                    // Email Login
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Operative Email", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TacticalOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TacticalOrange,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", fontSize = 11.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TacticalOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TacticalOrange,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.loginWithEmail(emailInput, emailInput.substringBefore("@")) },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("email_login_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("AUTHENTICATE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GunmetalDark)
                    }
                }

                2 -> {
                    // Register
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Operative Call-Sign", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_callsign_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TacticalOrange,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TacticalOrange,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.loginWithEmail(emailInput, usernameInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("register_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ENLIST OPERATIVE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GunmetalDark)
                    }
                }
            }
        }
    }
}
