package com.example.ui.screens

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.LootItem
import com.example.game.GameState
import com.example.game.engine.GameRenderer3D
import com.example.game.entities.GrenadeType
import com.example.ui.components.CompassBar
import com.example.ui.components.InteractionPrompt
import com.example.ui.components.TacticalMinimap
import com.example.ui.components.VirtualJoystick
import com.example.ui.components.WeaponCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GunmetalDark
import com.example.ui.theme.HealthGreen
import com.example.ui.theme.ShieldBlue
import com.example.ui.theme.TacticalOrange
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenState
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val session = viewModel.activeSession.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    if (session == null) {
        viewModel.navigateTo(ScreenState.MAIN_MENU)
        return
    }

    var isPaused by remember { mutableStateOf(false) }
    val userSettings by viewModel.settings.collectAsStateWithLifecycle()

    val aliveCount by session.aliveCount.collectAsStateWithLifecycle()
    val kills by session.playerKills.collectAsStateWithLifecycle()
    val killFeed by session.killFeed.collectAsStateWithLifecycle()
    val gameState by session.gameState.collectAsStateWithLifecycle()
    val nearestLoot by session.nearestLoot.collectAsStateWithLifecycle()
    val nearestVehicle by session.nearestVehicle.collectAsStateWithLifecycle()

    BackHandler {
        isPaused = true
    }

    // Check Match Completion
    LaunchedEffect(gameState) {
        if (gameState == GameState.VICTORY) {
            viewModel.onMatchFinished(
                isVictory = true,
                kills = kills,
                damage = session.totalDamageDealt,
                placement = 1
            )
        } else if (gameState == GameState.DEFEAT) {
            viewModel.onMatchFinished(
                isVictory = false,
                kills = kills,
                damage = session.totalDamageDealt,
                placement = session.matchPlacement
            )
        }
    }

    // 60 FPS Game Loop
    LaunchedEffect(isPaused) {
        var lastTime = System.nanoTime()
        while (isActive && !isPaused && gameState == GameState.IN_PROGRESS) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastTime = now

            session.update(dt)
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }

    val renderer = remember { GameRenderer3D(context, session) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("in_game_screen")
    ) {
        // 1. OpenGL ES 3D Viewport
        AndroidView(
            factory = { ctx ->
                GLSurfaceView(ctx).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(renderer)
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Right-side touch drag area for Camera Aim/Look
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(userSettings.cameraSensitivity, session.player.isAimingDownSights) {
                    val sens = if (session.player.isAimingDownSights) {
                        userSettings.aimSensitivity * 0.16f
                    } else {
                        userSettings.cameraSensitivity * 0.22f
                    }
                    detectDragGestures { change, dragAmount ->
                        // Only act on right half of screen
                        if (change.position.x > size.width * 0.35f) {
                            change.consume()
                            session.player.yaw -= dragAmount.x * sens
                            session.player.pitch += dragAmount.y * sens
                        }
                    }
                }
        )

        // 3. Red Outside-Zone Damage Vignette
        val isOutsideZone = !session.safeZone.isInsideSafeZone(session.player.position)
        if (isOutsideZone) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color(0x77FF2A4B))
                        )
                    )
            )
        }

        // 4. Center Crosshair
        Canvas(modifier = Modifier.size(36.dp).align(Alignment.Center)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val spread = if (session.player.isAimingDownSights) 8f else 16f
            val crossColor = if (session.isFiring) TacticalOrange else Color(0xCCFFFFFF)

            // Top, Bottom, Left, Right crosshair ticks
            drawLine(crossColor, Offset(c.x, c.y - spread - 8f), Offset(c.x, c.y - spread), 2f)
            drawLine(crossColor, Offset(c.x, c.y + spread), Offset(c.x, c.y + spread + 8f), 2f)
            drawLine(crossColor, Offset(c.x - spread - 8f, c.y), Offset(c.x - spread, c.y), 2f)
            drawLine(crossColor, Offset(c.x + spread, c.y), Offset(c.x + spread + 8f, c.y), 2f)
            drawCircle(crossColor, 2f, c)
        }

        // 5. HUD OVERLAY
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // TOP BAR HUD: Minimap, Compass, Stats, Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top-Left: Tactical Minimap
                TacticalMinimap(
                    session = session,
                    size = 95.dp
                )

                // Top-Center: Compass & Match Stats
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CompassBar(currentHeadingDeg = session.player.yaw)

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Alive Counter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xBB11161B))
                                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ALIVE: $aliveCount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TacticalOrange
                            )
                        }

                        // Kills Counter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xBB11161B))
                                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "KILLS: $kills",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // Top-Right: Killfeed & Pause Button
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x8811161B))
                            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { isPaused = true }
                            .testTag("pause_game_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Killfeed items (up to 3)
                    killFeed.take(3).forEach { item ->
                        Text(
                            text = "${item.killer} ⚔ ${item.victim}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isPlayerInvolved) TacticalOrange else Color(0xDDFFFFFF),
                            modifier = Modifier
                                .background(Color(0x88000000), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Contextual Interaction Prompt (Loot / Vehicle)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                InteractionPrompt(
                    loot = nearestLoot,
                    vehicle = nearestVehicle,
                    isDriving = session.player.isDriving,
                    onLootClick = { session.pickUpNearestLoot() },
                    onVehicleClick = { session.toggleVehicleEntry() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // BOTTOM CONTROLS BAR: Left Joystick, Center Health/Shield, Right Combat Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Bottom-Left: Virtual Joystick & Sprint lock
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Sprint toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (session.player.isSprinting) TacticalOrange else Color(0x6611161B))
                            .clickable { session.player.isSprinting = !session.player.isSprinting }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("sprint_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Sprint",
                                tint = if (session.player.isSprinting) GunmetalDark else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SPRINT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (session.player.isSprinting) GunmetalDark else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    VirtualJoystick(
                        size = 130.dp,
                        onMove = { x, y ->
                            session.moveJoystickX = x
                            session.moveJoystickY = y
                        }
                    )
                }

                // Center-Bottom: Health, Shield, and Medical Items
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(220.dp)
                ) {
                    // Shield Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = ShieldBlue, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("ARMOR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ShieldBlue)
                        }
                        Text("${session.player.shield.toInt()}/${session.player.maxShield.toInt()}", fontSize = 9.sp, color = Color.White)
                    }
                    LinearProgressIndicator(
                        progress = { (session.player.shield / session.player.maxShield).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ShieldBlue,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Health Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalHospital, contentDescription = null, tint = HealthGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("HEALTH", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HealthGreen)
                        }
                        Text("${session.player.health.toInt()}/100", fontSize = 9.sp, color = Color.White)
                    }
                    LinearProgressIndicator(
                        progress = { (session.player.health / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (session.player.health <= 25f) DangerRed else HealthGreen,
                        trackColor = Color(0x33FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Medical and Grenade Toss Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x9911161B))
                                .border(1.dp, HealthGreen, RoundedCornerShape(6.dp))
                                .clickable {
                                    session.player.heal(50f, 25f)
                                    viewModel.soundManager.playSound("click")
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("use_medkit_button")
                        ) {
                            Text("+50 MEDKIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HealthGreen)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x9911161B))
                                .border(1.dp, DangerRed, RoundedCornerShape(6.dp))
                                .clickable {
                                    session.throwGrenade(GrenadeType.FRAG)
                                    viewModel.soundManager.playSound("click")
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("throw_grenade_button")
                        ) {
                            Text("💣 FRAG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                        }
                    }
                }

                // Bottom-Right: Fire, ADS, Jump, Crouch, Weapon HUD
                Column(horizontalAlignment = Alignment.End) {
                    // Weapon Card
                    WeaponCard(
                        weapons = session.player.weapons,
                        currentWeaponIndex = session.player.currentWeaponIndex,
                        onSwitchWeapon = { session.player.switchWeapon(it) },
                        onReload = {
                            session.player.reloadActiveWeapon()
                            viewModel.soundManager.playSound("reload")
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Combat Action Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Crouch
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (session.player.isCrouched) TacticalOrange else Color(0x9911161B))
                                .border(1.dp, Color(0x55FFFFFF), CircleShape)
                                .clickable { session.player.isCrouched = !session.player.isCrouched }
                                .testTag("crouch_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("CROUCH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Jump
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0x9911161B))
                                .border(1.dp, Color(0x55FFFFFF), CircleShape)
                                .clickable {
                                    session.player.position.y += 1.2f
                                    viewModel.soundManager.playSound("click")
                                }
                                .testTag("jump_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Jump", tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        // ADS (Aim Down Sights)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (session.player.isAimingDownSights) TacticalOrange else Color(0x9911161B))
                                .border(1.5.dp, TacticalOrange, CircleShape)
                                .clickable { session.player.isAimingDownSights = !session.player.isAimingDownSights }
                                .testTag("aim_ads_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Aim",
                                tint = if (session.player.isAimingDownSights) GunmetalDark else TacticalOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Big FIRE Button
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(TacticalOrange)
                                .border(2.5.dp, Color.White, CircleShape)
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val down = event.changes.any { it.pressed }
                                            session.isFiring = down
                                        }
                                    }
                                }
                                .testTag("fire_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FIRE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GunmetalDark
                            )
                        }
                    }
                }
            }
        }

        // 6. Pause Dialog Overlay
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GunmetalDark)
                        .border(1.5.dp, TacticalOrange, RoundedCornerShape(14.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("MISSION PAUSED", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TacticalOrange),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESUME BATTLE", fontWeight = FontWeight.Bold, color = GunmetalDark)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isPaused = false
                            viewModel.navigateTo(ScreenState.SETTINGS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CONTROLS & SETTINGS", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isPaused = false
                            viewModel.leaveMatch()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LEAVE MATCH", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
