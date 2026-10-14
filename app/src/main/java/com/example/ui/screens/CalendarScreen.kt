package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEventEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    events: List<CalendarEventEntity>,
    selectedProvider: String,
    onProviderSelected: (String) -> Unit,
    onCreateEvent: (title: String, desc: String, startMillis: Long, endMillis: Long, location: String, provider: String, reminder: Int) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onTestAlert: (CalendarEventEntity) -> Unit,
    onExportToSystem: (CalendarEventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredEvents = remember(events, selectedProvider) {
        if (selectedProvider == "ALL") events
        else events.filter { it.provider.equals(selectedProvider, ignoreCase = true) }
    }

    val googleCount = events.count { it.provider.equals("GOOGLE", ignoreCase = true) }
    val outlookCount = events.count { it.provider.equals("OUTLOOK", ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgentVoidDark)
            .padding(top = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ИНТЕГРАЦИЯ КАЛЕНДАРЕЙ",
                    color = AgentTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Google: $googleCount • Outlook: $outlookCount",
                    color = AgentCyan,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgentCyan,
                    contentColor = AgentVoidDark
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_calendar_event_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Создать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Voice Command Tip
        Surface(
            color = AgentSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = AgentAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Голосовое управление: «Запланируй созвон в Google на 15:00» или «Удали встречу из Outlook»",
                    color = AgentTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // Provider Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProviderFilterChip(
                label = "Все календари (${events.size})",
                isSelected = selectedProvider == "ALL",
                color = AgentCyan,
                onClick = { onProviderSelected("ALL") },
                modifier = Modifier.weight(1f)
            )
            ProviderFilterChip(
                label = "Google ($googleCount)",
                isSelected = selectedProvider == "GOOGLE",
                color = Color(0xFF4285F4),
                onClick = { onProviderSelected("GOOGLE") },
                modifier = Modifier.weight(1f)
            )
            ProviderFilterChip(
                label = "Outlook ($outlookCount)",
                isSelected = selectedProvider == "OUTLOOK",
                color = Color(0xFF0078D4),
                onClick = { onProviderSelected("OUTLOOK") },
                modifier = Modifier.weight(1f)
            )
        }

        // Events List
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        tint = AgentTextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "В данном календаре нет событий",
                        color = AgentTextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Добавьте голосом или кнопкой «Создать»",
                        color = AgentTextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    CalendarEventCard(
                        event = event,
                        onDelete = { onDeleteEvent(event.id) },
                        onTestAlert = { onTestAlert(event) },
                        onExport = { onExportToSystem(event) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            defaultProvider = if (selectedProvider != "ALL") selectedProvider else "GOOGLE",
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, start, end, loc, prov, rem ->
                onCreateEvent(title, desc, start, end, loc, prov, rem)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProviderFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) color.copy(alpha = 0.25f) else AgentSurfaceVariantDark,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isSelected) color else AgentCardStrokeDark,
                RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) color else AgentTextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CalendarEventCard(
    event: CalendarEventEntity,
    onDelete: () -> Unit,
    onTestAlert: () -> Unit,
    onExport: () -> Unit
) {
    val isGoogle = event.provider.equals("GOOGLE", ignoreCase = true)
    val providerColor = if (isGoogle) Color(0xFF4285F4) else Color(0xFF0078D4)
    val providerName = if (isGoogle) "Google Calendar" else "Outlook Calendar"

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))

    val startDate = Date(event.startTimeMillis)
    val endDate = Date(event.endTimeMillis)

    Surface(
        color = AgentSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, providerColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = providerColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isGoogle) Icons.Default.CalendarMonth else Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = providerColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = providerName,
                            color = providerColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onTestAlert,
                        modifier = Modifier.size(30.dp).testTag("test_alert_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Тест уведомления",
                            tint = AgentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onExport,
                        modifier = Modifier.size(30.dp).testTag("export_event_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "В системный календарь",
                            tint = AgentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp).testTag("delete_event_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить событие",
                            tint = AgentRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                color = AgentTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.description,
                    color = AgentTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = AgentCardStrokeDark, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AgentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${timeFormat.format(startDate)} – ${timeFormat.format(endDate)} (${dateFormat.format(startDate)})",
                        color = AgentTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (event.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AgentTextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location,
                            color = AgentTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateEventDialog(
    defaultProvider: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, startMillis: Long, endMillis: Long, location: String, provider: String, reminder: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Онлайн") }
    var provider by remember { mutableStateOf(defaultProvider) }
    var hourOffset by remember { mutableStateOf(2) } // default starts in 2 hours

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgentSurfaceDark,
        title = {
            Text(
                text = "Запланировать встречу",
                color = AgentCyan,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Тема встречи") },
                    placeholder = { Text("Синхронизация по проекту") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Provider Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { provider = "GOOGLE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (provider == "GOOGLE") Color(0xFF4285F4) else AgentSurfaceVariantDark,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Google", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { provider = "OUTLOOK" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (provider == "OUTLOOK") Color(0xFF0078D4) else AgentSurfaceVariantDark,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Outlook", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Место или ссылка") },
                    placeholder = { Text("Google Meet / Teams / Офис") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Описание") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.HOUR_OF_DAY, hourOffset)
                        val start = cal.timeInMillis
                        val end = start + 3600000L // 1 hour
                        onConfirm(title, desc, start, end, location, provider, 15)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgentCyan,
                    contentColor = AgentVoidDark
                )
            ) {
                Text("Запланировать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = AgentTextSecondary)
            }
        }
    )
}
