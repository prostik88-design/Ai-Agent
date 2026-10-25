package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationWorkflowEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.service.BatteryState

@Composable
fun AgentHeader(
    isLocalOnly: Boolean,
    activeMcpCount: Int,
    isProcessing: Boolean,
    batteryState: BatteryState,
    activeWorkflowsCount: Int = 4,
    onPrivacyToggle: () -> Unit,
    onOpenDrawer: () -> Unit
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
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.size(34.dp).testTag("open_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Боковое меню",
                            tint = AgentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
                            .size(12.dp)
                            .scale(if (isProcessing) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(if (isProcessing) AgentAmber else AgentEmerald)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HERMES AGENT",
                                color = AgentCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Surface(
                                color = AgentSurfaceVariantDark,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PROCESS AI",
                                    color = AgentAmber,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            text = if (isProcessing) "⚙️ Оркестрация процессов..." else "⚡ $activeWorkflowsCount рутины активны",
                            color = AgentTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Privacy, Workflows & Battery Badges (Compact horizontal bar)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LocalAi compact horizontal button
                    Surface(
                        color = if (isLocalOnly) AgentCyan.copy(alpha = 0.18f) else AgentAmber.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(24.dp)
                            .clickable { onPrivacyToggle() }
                            .border(
                                1.dp,
                                if (isLocalOnly) AgentCyan.copy(alpha = 0.8f) else AgentAmber.copy(alpha = 0.8f),
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("local_ai_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocalOnly) Icons.Default.Lock else Icons.Default.CloudQueue,
                                contentDescription = "Режим ИИ",
                                tint = if (isLocalOnly) AgentCyan else AgentAmber,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isLocalOnly) "LocalAi" else "Cloud",
                                color = if (isLocalOnly) AgentCyan else AgentAmber,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Workflows count badge
                    Surface(
                        color = AgentEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(24.dp)
                            .border(1.dp, AgentEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = AgentEmerald,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$activeWorkflowsCount",
                                color = AgentEmerald,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Battery Badge
                    val batteryColor = when {
                        batteryState.isCharging -> AgentCyan
                        batteryState.isSuspended -> AgentAmber
                        batteryState.levelPercent <= 20 -> AgentRed
                        else -> AgentEmerald
                    }
                    val batteryIcon = when {
                        batteryState.isCharging -> Icons.Default.BatteryChargingFull
                        batteryState.isSuspended || batteryState.levelPercent <= 20 -> Icons.Default.BatteryAlert
                        else -> Icons.Default.BatteryStd
                    }

                    Surface(
                        color = batteryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(24.dp)
                            .border(1.dp, batteryColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = batteryIcon,
                                contentDescription = "Статус батареи",
                                tint = batteryColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${batteryState.levelPercent}%",
                                color = batteryColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
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

@Composable
fun LowPowerStatusBar(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = batteryState.isSuspended,
        modifier = modifier
    ) {
        Surface(
            color = AgentAmber.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, AgentAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = AgentAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "⚡ РЕЖИМ ЭНЕРГОСБЕРЕЖЕНИЯ (${batteryState.levelPercent}%)",
                        color = AgentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Фоновые процессы локального LLM и MCP-синк приостановлены. Срочные события календаря активны.",
                        color = AgentTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryTelemetryCard(
    batteryState: BatteryState,
    onToggleSimulatedLowPower: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        batteryState.isCharging -> AgentCyan
        batteryState.isSuspended -> AgentAmber
        batteryState.levelPercent <= 20 -> AgentRed
        else -> AgentEmerald
    }

    Surface(
        color = AgentSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (batteryState.isSuspended) AgentAmber.copy(alpha = 0.6f) else AgentCardStrokeDark,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (batteryState.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "ВТОРИЧНЫЙ ОПТИМИЗАТОР ПИТАНИЯ",
                            color = AgentTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (batteryState.isCharging) "Идет зарядка" else if (batteryState.isSuspended) "Энергосбережение включено (щадящий режим)" else "Автоматический контроль активен",
                            color = barColor,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = barColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${batteryState.levelPercent}%",
                        color = barColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { (batteryState.levelPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = AgentSurfaceVariantDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Task State Matrix
            Text(
                text = "СОСТОЯНИЕ ФОНОВЫХ ПРОЦЕССОВ:",
                color = AgentTextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            TaskStatusRow(
                title = "🧠 Локальные процессы LLM",
                status = if (batteryState.isSuspended) "ПРИОСТАНОВЛЕН" else "АКТИВЕН",
                isActive = !batteryState.isSuspended
            )
            TaskStatusRow(
                title = "🔄 Фоновая синхронизация MCP",
                status = if (batteryState.isSuspended) "ПАУЗА" else "АКТИВЕН",
                isActive = !batteryState.isSuspended
            )
            TaskStatusRow(
                title = "⏰ Срочные уведомления календаря",
                status = "ЗАЩИЩЕНЫ (АКТИВНЫ)",
                isActive = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AgentCardStrokeDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Simulation Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Симуляция низкого заряда (<20%)",
                        color = AgentTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Проверить остановку LLM и MCP в эмуляторе",
                        color = AgentTextTertiary,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = batteryState.isSimulatedLowPower,
                    onCheckedChange = onToggleSimulatedLowPower,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AgentVoidDark,
                        checkedTrackColor = AgentAmber,
                        uncheckedThumbColor = AgentTextTertiary,
                        uncheckedTrackColor = AgentSurfaceVariantDark
                    ),
                    modifier = Modifier.testTag("toggle_battery_simulation")
                )
            }
        }
    }
}

@Composable
fun TaskStatusRow(
    title: String,
    status: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = AgentTextSecondary,
            fontSize = 11.sp
        )
        Surface(
            color = if (isActive) AgentEmerald.copy(alpha = 0.15f) else AgentAmber.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = status,
                color = if (isActive) AgentEmerald else AgentAmber,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * WorkflowCard Composable
 * Displays a visual representation of a task pipeline, including:
 * - A dynamic trigger icon and label
 * - Visual pipeline representation showing flow of actions
 * - Action description
 * - Timestamp of the last execution
 */
@Composable
fun WorkflowCard(
    workflow: AutomationWorkflowEntity,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true,
    onToggle: (Boolean) -> Unit = {},
    onExecute: () -> Unit = {},
    onShowHistory: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    WorkflowCard(
        title = workflow.title,
        triggerType = workflow.triggerType,
        triggerValue = workflow.triggerValue,
        actionDescription = workflow.actionsSummary,
        lastExecutionTimeMillis = workflow.lastRunMillis,
        modifier = modifier,
        description = workflow.description,
        category = workflow.category,
        isEnabled = workflow.isEnabled,
        executionCount = workflow.executionCount,
        isDarkTheme = isDarkTheme,
        onToggle = onToggle,
        onExecute = onExecute,
        onShowHistory = onShowHistory,
        onDelete = onDelete
    )
}

@Composable
fun WorkflowCard(
    title: String,
    triggerType: String,
    triggerValue: String,
    actionDescription: String,
    lastExecutionTimeMillis: Long?,
    modifier: Modifier = Modifier,
    description: String = "",
    category: String = "ROUTINE",
    isEnabled: Boolean = true,
    executionCount: Int = 0,
    isDarkTheme: Boolean = true,
    onToggle: (Boolean) -> Unit = {},
    onExecute: () -> Unit = {},
    onShowHistory: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val categoryColor = when (category.uppercase()) {
        "CALENDAR" -> AgentCyan
        "NOTES" -> AgentAmber
        "OPTIMIZER", "BATTERY" -> AgentEmerald
        else -> Color(0xFF818CF8) // Indigo
    }

    // Dynamic trigger icon
    val triggerIcon: ImageVector = when {
        triggerType.contains("CALENDAR", ignoreCase = true) || triggerValue.contains("встреч", ignoreCase = true) -> Icons.Default.Event
        triggerType.contains("BATTERY", ignoreCase = true) || triggerType.contains("POWER", ignoreCase = true) -> Icons.Default.BatteryChargingFull
        triggerType.contains("TIME", ignoreCase = true) || triggerType.contains("SCHEDULE", ignoreCase = true) || triggerValue.contains(":", ignoreCase = true) -> Icons.Default.Schedule
        triggerType.contains("VOICE", ignoreCase = true) -> Icons.Default.Mic
        else -> Icons.Default.Bolt
    }

    val dateFormat = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }

    Surface(
        color = if (isDarkTheme) AgentSurfaceDark else Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEnabled) categoryColor.copy(alpha = 0.5f) else (if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1)),
                RoundedCornerShape(14.dp)
            )
            .testTag("workflow_card_${title.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category, Status, Switch & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = category.uppercase(),
                            color = categoryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = if (isEnabled) AgentEmerald.copy(alpha = 0.15f) else AgentTextTertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isEnabled) "АКТИВЕН" else "ПАУЗА",
                            color = if (isEnabled) AgentEmerald else AgentTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AgentCyan,
                            checkedTrackColor = AgentCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = AgentTextTertiary,
                            uncheckedTrackColor = AgentSurfaceVariantDark
                        ),
                        modifier = Modifier.testTag("workflow_switch")
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("workflow_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить пайплайн",
                            tint = AgentRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Description
            Text(
                text = title,
                color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("workflow_title")
            )

            if (description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. TRIGGER SECTION (Trigger icon & trigger value)
            Surface(
                color = if (isDarkTheme) AgentVoidDark else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = triggerIcon,
                        contentDescription = "Trigger Icon",
                        tint = AgentAmber,
                        modifier = Modifier
                            .size(15.dp)
                            .testTag("workflow_trigger_icon")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ТРИГГЕР: $triggerValue",
                        color = AgentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("workflow_trigger_value")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. VISUAL PIPELINE FLOW (Chained steps with visual connectors)
            Text(
                text = "ВИЗУАЛЬНЫЙ ПАЙПЛАЙН (TASK PIPELINE):",
                color = AgentTextTertiary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pipeline visual chips
            val actionSteps = actionDescription
                .split("➔", "->", ",", "|")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pipeline Trigger Node
                Surface(
                    color = AgentAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, AgentAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            tint = AgentAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Старт",
                            color = AgentAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = AgentTextTertiary,
                    modifier = Modifier.size(12.dp)
                )

                // Pipeline Step Nodes
                actionSteps.forEachIndexed { index, step ->
                    Surface(
                        color = categoryColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(1.dp, categoryColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${index + 1}. $step",
                                color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (index < actionSteps.size - 1) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. ACTION DESCRIPTION
            Text(
                text = "ДЕЙСТВИЯ: $actionDescription",
                color = if (isDarkTheme) AgentTextSecondary else Color(0xFF334155),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.testTag("action_description")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. TIMESTAMP OF LAST EXECUTION & RUN CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val lastExecText = if (lastExecutionTimeMillis != null && lastExecutionTimeMillis > 0L) {
                        "Последний запуск: ${dateFormat.format(Date(lastExecutionTimeMillis))}"
                    } else {
                        "Последний запуск: никогда"
                    }
                    Text(
                        text = lastExecText,
                        color = AgentTextTertiary,
                        fontSize = 10.sp,
                        modifier = Modifier.testTag("workflow_last_execution_timestamp")
                    )
                    Text(
                        text = "Выполнено: $executionCount раз",
                        color = AgentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onShowHistory,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("workflow_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = AgentCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("История", fontSize = 11.sp, color = AgentCyan)
                    }

                    Button(
                        onClick = onExecute,
                        colors = ButtonDefaults.buttonColors(containerColor = AgentCyan, contentColor = AgentVoidDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("workflow_execute_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Запустить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
