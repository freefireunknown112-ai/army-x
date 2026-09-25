package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TacticalOrange

@Composable
fun CompassBar(
    currentHeadingDeg: Float,
    modifier: Modifier = Modifier
) {
    val normHeading = ((-currentHeadingDeg % 360f + 360f) % 360f).toInt()

    Box(
        modifier = modifier
            .width(260.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x990A0E12))
            .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(6.dp))
            .testTag("compass_bar"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val centerX = width / 2f
            val pxPerDeg = width / 120f // 120-degree field of view visible

            for (deg in (normHeading - 60)..(normHeading + 60) step 15) {
                val wrapped = (deg % 360 + 360) % 360
                val x = centerX + (deg - normHeading) * pxPerDeg

                val isCardinal = wrapped % 45 == 0
                val tickHeight = if (isCardinal) size.height * 0.45f else size.height * 0.25f

                drawLine(
                    color = if (isCardinal) TacticalOrange else Color(0x66FFFFFF),
                    start = Offset(x, size.height - tickHeight),
                    end = Offset(x, size.height),
                    strokeWidth = if (isCardinal) 2f else 1.2f
                )

                if (isCardinal) {
                    val label = when (wrapped) {
                        0 -> "N"
                        45 -> "NE"
                        90 -> "E"
                        135 -> "SE"
                        180 -> "S"
                        225 -> "SW"
                        270 -> "W"
                        315 -> "NW"
                        else -> "$wrapped"
                    }
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(label, x, 22f, paint)
                }
            }

            // Center indicator notch
            drawLine(
                color = TacticalOrange,
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = 3f
            )
        }

        // Bearing number badge
        Text(
            text = "$normHeading°",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TacticalOrange,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
