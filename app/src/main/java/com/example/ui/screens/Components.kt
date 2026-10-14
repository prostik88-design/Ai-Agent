package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun AgentHeader(
    isLocalOnly: Boolean,
    activeMcpCount: Int,
    isProcessing: Boolean,
    onPrivacyToggle: () -> Unit
) {
    Surface(
        color = AgentSurfaceDark,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = AgentCardStrokeDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pulsing status dot
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.25f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .scale(if (isProcessing) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(if (isProcessing) AgentAmber else AgentEmerald)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HERMES AGENT",
                                color = AgentCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AgentSurfaceVariantDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "OPENCLAW v2.4",
                                    color = AgentAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isProcessing) "⚙️ Агент выполняет цепочку MCP..." else "🟢 Онлайн • 100% Автономность",
                            color = AgentTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Privacy Badge & MCP count
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = if (isLocalOnly) AgentCyanDark.copy(alpha = 0.25f) else AgentAmberDark.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clickable { onPrivacyToggle() }
                            .border(
                                1.dp,
                                if (isLocalOnly) AgentCyan else AgentAmber,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocalOnly) Icons.Default.Lock else Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (isLocalOnly) AgentCyan else AgentAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLocalOnly) "LOCAL AI" else "HYBRID",
                                color = if (isLocalOnly) AgentCyan else AgentAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = AgentSurfaceVariantDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, AgentCardStrokeDark, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = AgentEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$activeMcpCount MCP",
                                color = AgentEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceMicOrb(
    isListening: Boolean,
    soundLevel: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_orb")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val currentScale = if (isListening) (1.0f + soundLevel * 0.4f).coerceIn(1.0f, 1.4f) else 1.0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(72.dp)
            .scale(currentScale)
            .clickable(onClick = onClick)
            .testTag("voice_mic_button")
    ) {
        // Outer glow
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(glowPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AgentCyan.copy(alpha = 0.5f),
                                AgentViolet.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Inner circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isListening) {
                        Brush.linearGradient(listOf(AgentCyan, AgentViolet))
                    } else {
                        Brush.linearGradient(listOf(AgentSurfaceVariantDark, AgentSurfaceDark))
                    }
                )
                .border(
                    width = 2.dp,
                    color = if (isListening) AgentCyan else AgentCardStrokeDark,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = "Голосовая команда",
                tint = if (isListening) AgentVoidDark else AgentCyan,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun HermesReasoningAccordion(
    reasoningTrace: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = AgentVoidDark.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AgentCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = AgentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hermes Chain-of-Thought (Рассуждение)",
                        color = AgentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = AgentTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = AgentCardStrokeDark, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reasoningTrace,
                        color = AgentTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
