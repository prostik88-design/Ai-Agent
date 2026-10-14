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
import com.example.data.model.NoteEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesAndSettingsScreen(
    notes: List<NoteEntity>,
    useLocalModelOnly: Boolean,
    isTtsEnabled: Boolean,
    telegramBotToken: String,
    telegramChatId: String,
    onToggleLocalModel: (Boolean) -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onSaveTelegramCredentials: (String, String) -> Unit,
    onSendTelegramTest: () -> Unit,
    onCreateNote: (String, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = Privacy & AI, 2 = Messengers
    var showCreateNoteDialog by remember { mutableStateOf(false) }

    var tokenInput by remember { mutableStateOf(telegramBotToken) }
    var chatIdInput by remember { mutableStateOf(telegramChatId) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgentVoidDark)
            .padding(top = 8.dp)
    ) {
        // Top SubTabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = AgentVoidDark,
            contentColor = AgentCyan,
            divider = { Divider(color = AgentCardStrokeDark) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Заметки (${notes.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Приватность & ИИ", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Мессенджеры", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedSubTab) {
            0 -> {
                // Notes View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "БАЗА ЗНАНИЙ И УМНЫЕ ЗАМЕТКИ",
                            color = AgentTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = { showCreateNoteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AgentEmerald,
                                contentColor = AgentVoidDark
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("add_note_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Новая заметка", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (notes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Заметок пока нет. Скажите: «Запиши заметку: согласовать проект» или нажмите «Новая заметка».",
                                color = AgentTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(notes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    onDelete = { onDeleteNote(note.id) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Privacy & Local AI Settings
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
                ) {
                    item {
                        Surface(
                            color = AgentSurfaceDark,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AgentCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = AgentCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "100% Локальный ИИ (Zero Telemetry)",
                                                color = AgentTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Полная конфиденциальность и офлайн",
                                                color = AgentCyan,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = useLocalModelOnly,
                                        onCheckedChange = onToggleLocalModel,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = AgentVoidDark,
                                            checkedTrackColor = AgentCyan
                                        ),
                                        modifier = Modifier.testTag("toggle_privacy_mode")
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "В этом режиме все голосовые команды, заметки, календарные события и цепочки рассуждений обрабатываются встроенным локальным NLP-движком непосредственно на вашем смартфоне. Данные не передаются на внешние серверы.",
                                    color = AgentTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            color = AgentSurfaceDark,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = AgentAmber,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Голосовой ответ (Text-To-Speech)",
                                                color = AgentTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Озвучивание результатов вслух",
                                                color = AgentTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isTtsEnabled,
                                        onCheckedChange = onToggleTts,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = AgentVoidDark,
                                            checkedTrackColor = AgentAmber
                                        ),
                                        modifier = Modifier.testTag("toggle_tts")
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            color = AgentSurfaceDark,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "АРХИТЕКТУРА И СТАТУС СИСТЕМЫ",
                                    color = AgentAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Движок: OpenClaw / Hermes Autonomous Agent\n" +
                                            "• Хранилище: Локальная SQLite Room (AES-Ready)\n" +
                                            "• Календари: Google Calendar API + Microsoft Graph (Outlook)\n" +
                                            "• Протокол инструментов: Model Context Protocol (MCP v2.0)\n" +
                                            "• Речевой ввод: Android SpeechRecognizer STT & TTS",
                                    color = AgentTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Messenger / Telegram Settings
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
                ) {
                    item {
                        Surface(
                            color = AgentSurfaceDark,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF29B6F6).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color(0xFF29B6F6),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Telegram Bot Интеграция",
                                            color = AgentTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Мгновенные оповещения и дайджесты встреч",
                                            color = Color(0xFF29B6F6),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = tokenInput,
                                    onValueChange = {
                                        tokenInput = it
                                        onSaveTelegramCredentials(tokenInput, chatIdInput)
                                    },
                                    label = { Text("Telegram Bot Token") },
                                    placeholder = { Text("123456789:ABCdefGHIjklmn...") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = AgentTextPrimary,
                                        unfocusedTextColor = AgentTextPrimary,
                                        focusedBorderColor = AgentCyan,
                                        unfocusedBorderColor = AgentCardStrokeDark
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_token_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = chatIdInput,
                                    onValueChange = {
                                        chatIdInput = it
                                        onSaveTelegramCredentials(tokenInput, chatIdInput)
                                    },
                                    label = { Text("Telegram Chat ID") },
                                    placeholder = { Text("Например: 987654321 или @mychannel") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = AgentTextPrimary,
                                        unfocusedTextColor = AgentTextPrimary,
                                        focusedBorderColor = AgentCyan,
                                        unfocusedBorderColor = AgentCardStrokeDark
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("telegram_chat_id_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = onSendTelegramTest,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF29B6F6),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("send_telegram_test_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Отправить тестовый сигнал в Telegram", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateNoteDialog) {
        CreateNoteDialog(
            onDismiss = { showCreateNoteDialog = false },
            onConfirm = { title, content, tags ->
                onCreateNote(title, content, tags)
                showCreateNoteDialog = false
            }
        )
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())

    Surface(
        color = AgentSurfaceDark,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AgentEmerald.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    color = AgentTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).testTag("delete_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить заметку",
                        tint = AgentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.content,
                color = AgentTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            if (note.actionItems.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AgentSurfaceVariantDark,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = AgentEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Действия: ${note.actionItems}",
                            color = AgentEmerald,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${note.tags.ifBlank { "Заметка" }}",
                    color = AgentCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = dateFormat.format(Date(note.createdAtMillis)),
                    color = AgentTextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Планы,Идеи") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgentSurfaceDark,
        title = {
            Text(text = "Создать заметку", color = AgentCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Текст заметки") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Теги") },
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
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val finalTitle = title.ifBlank { "Заметка от " + SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date()) }
                        onConfirm(finalTitle, content, tags)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgentEmerald, contentColor = AgentVoidDark)
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = AgentTextSecondary)
            }
        }
    )
}
