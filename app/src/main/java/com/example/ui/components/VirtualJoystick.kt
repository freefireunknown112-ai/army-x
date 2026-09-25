package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    onMove: (x: Float, y: Float) -> Unit
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(size)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                val radius = size.toPx() / 2f
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(radius, radius)
                        val delta = offset - center
                        val dist = delta.getDistance()
                        val clamped = if (dist > radius) delta * (radius / dist) else delta
                        dragOffset = clamped
                        onMove(clamped.x / radius, clamped.y / radius)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val radiusPx = size.toPx() / 2f
                        val newOffset = dragOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clamped = if (dist > radiusPx) newOffset * (radiusPx / dist) else newOffset
                        dragOffset = clamped
                        onMove(clamped.x / radiusPx, clamped.y / radiusPx)
                    },
                    onDragEnd = {
                        dragOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f
            val knobRadius = outerRadius * 0.38f

            // Outer Base Ring
            drawCircle(
                color = Color(0x33FFFFFF),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = Color(0x66FF6600),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3f)
            )

            // Inner guidelines
            drawLine(
                color = Color(0x22FFFFFF),
                start = Offset(center.x - outerRadius * 0.8f, center.y),
                end = Offset(center.x + outerRadius * 0.8f, center.y),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0x22FFFFFF),
                start = Offset(center.x, center.y - outerRadius * 0.8f),
                end = Offset(center.x, center.y + outerRadius * 0.8f),
                strokeWidth = 2f
            )

            // Center Knob
            val knobCenter = center + dragOffset
            drawCircle(
                color = Color(0xDDFF6600),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = Color.White,
                radius = knobRadius * 0.45f,
                center = knobCenter
            )
        }
    }
}
