package com.ebookfrenzy.examen02v3



import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CanvasView(currentPosition: Position) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRoom(this)
        drawDevicePosition(this, currentPosition)
    }
}

private fun drawRoom(drawScope: DrawScope) {
    with(drawScope) {
        // Dibujar el aula como un rectángulo
        val roomWidth = size.width * 0.8f
        val roomHeight = size.height * 0.8f
        val topLeft = Offset((size.width - roomWidth) / 2, (size.height - roomHeight) / 2)

        drawRoundRect(
            color = Color.LightGray,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(roomWidth, roomHeight),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 4f)
        )
    }
}

private fun drawDevicePosition(drawScope: DrawScope, position: Position) {
    with(drawScope) {
        // Convertir la posición del dispositivo a coordenadas del lienzo
        val roomWidth = size.width * 0.8f
        val roomHeight = size.height * 0.8f
        val topLeft = Offset((size.width - roomWidth) / 2, (size.height - roomHeight) / 2)

        val x = topLeft.x + (position.x * roomWidth / 5.0f).toFloat()
        val y = topLeft.y + (position.y * roomHeight / 5.0f).toFloat()

        // Dibujar la posición del dispositivo como un círculo
        drawCircle(
            color = Color.Red,
            radius = 20f,
            center = Offset(x, y)
        )
    }
}
