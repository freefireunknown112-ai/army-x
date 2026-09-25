package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameSession
import com.example.ui.theme.TacticalOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalMinimap(
    session: GameSession,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp
) {
    val remainingTime = session.safeZone.getRemainingTime()
    val isShrinking = session.safeZone.isShrinking

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xCC0E1216))
            .border(2.dp, TacticalOrange.copy(alpha = 0.8f), CircleShape)
            .testTag("tactical_minimap")
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = size.toPx() / 2f
            val scale = radius / 75f // Map scale: 75m visible radius

            // Grid radar circles
            drawCircle(Color(0x22FFFFFF), radius = radius * 0.65f, style = Stroke(width = 1f))
            drawCircle(Color(0x22FFFFFF), radius = radius * 0.35f, style = Stroke(width = 1f))

            // Crosshair lines
            drawLine(Color(0x22FFFFFF), Offset(center.x, 0f), Offset(center.x, size.toPx()), 1f)
            drawLine(Color(0x22FFFFFF), Offset(0f, center.y), Offset(size.toPx(), center.y), 1f)

            // Safe Zone Circle relative to player
            val zoneCenter = session.safeZone.currentCenter
            val pPos = session.player.position
            val zOffX = (zoneCenter.x - pPos.x) * scale
            val zOffZ = (zoneCenter.z - pPos.z) * scale
            val zScreenCenter = Offset(center.x + zOffX, center.y + zOffZ)

            drawCircle(
                color = Color(0xBB9C27B0),
                radius = session.safeZone.currentRadius * scale,
                center = zScreenCenter,
                style = Stroke(width = 2.5f)
            )

            // Vehicles blips (amber squares)
            for (veh in session.map.vehicles) {
                val vx = center.x + (veh.x - pPos.x) * scale
                val vz = center.y + (veh.z - pPos.z) * scale
                if (Offset(vx - center.x, vz - center.y).getDistance() < radius - 4f) {
                    drawRect(
                        color = Color(0xFFFFB300),
                        topLeft = Offset(vx - 3f, vz - 3f),
                        size = androidx.compose.ui.geometry.Size(6f, 6f)
                    )
                }
            }

            // Enemy Bots blips (red dots if within 30m)
            for (bot in session.bots) {
                if (!bot.isAlive) continue
                val dist = pPos.distanceTo(bot.position)
                if (dist <= 35f) {
                    val bx = center.x + (bot.position.x - pPos.x) * scale
                    val bz = center.y + (bot.position.z - pPos.z) * scale
                    if (Offset(bx - center.x, bz - center.y).getDistance() < radius - 4f) {
                        drawCircle(
                            color = Color(0xFFFF2A4B),
                            radius = 3.5f,
                            center = Offset(bx, bz)
                        )
                    }
                }
            }

            // Player Icon (Tactical Chevron pointing to player's yaw)
            val yawRad = Math.toRadians(session.player.yaw.toDouble())
            val tipX = center.x - sin(yawRad).toFloat() * 9f
            val tipY = center.y + cos(yawRad).toFloat() * 9f
            val leftX = center.x - sin(yawRad + 2.5).toFloat() * 7f
            val leftY = center.y + cos(yawRad + 2.5).toFloat() * 7f
            val rightX = center.x - sin(yawRad - 2.5).toFloat() * 7f
            val rightY = center.y + cos(yawRad - 2.5).toFloat() * 7f

            val chevron = Path().apply {
                moveTo(tipX, tipY)
                lineTo(leftX, leftY)
                lineTo(center.x, center.y)
                lineTo(rightX, rightY)
                close()
            }
            drawPath(chevron, color = Color(0xFF00E5FF))
        }

        // Zone countdown badge at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .background(if (isShrinking) Color(0xCCFF2A4B) else Color(0xAA000000), CircleShape)
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                text = if (isShrinking) "ZONE: ${remainingTime}s" else "NEXT: ${remainingTime}s",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
