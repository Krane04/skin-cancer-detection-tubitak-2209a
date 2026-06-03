package com.example.melonomscanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.melonomscanner.data.model.RiskLevel

@Composable
fun RiskIndicator(
    risk: RiskLevel,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val target = when (risk) {
        RiskLevel.LOW -> 0.2f
        RiskLevel.MEDIUM -> 0.5f
        RiskLevel.HIGH -> 0.75f
        RiskLevel.CRITICAL -> 0.95f
    }
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(900),
        label = "risk-progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(risk.color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Risk: ${risk.label}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            ) {
                val trackColor = Color.LightGray.copy(alpha = 0.35f)
                val stroke = Stroke(width = size.height, cap = StrokeCap.Round)
                drawLine(
                    color = trackColor,
                    start = Offset(size.height / 2, size.height / 2),
                    end = Offset(size.width - size.height / 2, size.height / 2),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        RiskLevel.LOW.color,
                        RiskLevel.MEDIUM.color,
                        RiskLevel.HIGH.color,
                        RiskLevel.CRITICAL.color
                    ),
                    startX = 0f,
                    endX = size.width
                )
                val end = (size.width - size.height) * progress + size.height / 2
                drawLine(
                    brush = brush,
                    start = Offset(size.height / 2, size.height / 2),
                    end = Offset(end.coerceAtLeast(size.height / 2), size.height / 2),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Güven: %${(confidence * 100).toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Düşük → Kritik",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = risk.recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
