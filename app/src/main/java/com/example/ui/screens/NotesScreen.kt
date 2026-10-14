package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun NotesScreen(
    notes: List<NoteEntity>,
    isDarkTheme: Boolean,
    onCreateNote: (String, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateNoteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AgentVoidDark else Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "БАЗА ЗНАНИЙ И УМНЫЕ ЗАМЕТКИ",
                    color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Всего заметок: ${notes.size}",
                    color = if (isDarkTheme) AgentEmerald else Color(0xFF059669),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showCreateNoteDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgentEmerald,
                    contentColor = AgentVoidDark
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).testTag("add_note_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = AgentTextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Заметок пока нет",
                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Скажите агенту: «Запиши заметку» или нажмите кнопку «Новая заметка»",
                        color = AgentTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        isDarkTheme = isDarkTheme,
                        onDelete = { onDeleteNote(note.id) }
                    )
                }
            }
        }
    }

    if (showCreateNoteDialog) {
        CreateNoteDialog(
            isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Surface(
        color = if (isDarkTheme) AgentSurfaceDark else Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(note.createdAtMillis)),
                        color = AgentTextTertiary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_note_${note.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить",
                        tint = AgentRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.content,
                color = if (isDarkTheme) AgentTextSecondary else Color(0xFF334155),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            if (note.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    note.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            color = AgentEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = AgentEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateNoteDialog(
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Новая заметка",
                color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    placeholder = { Text("Например: Идеи для релиза") },
                    modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Текст заметки") },
                    placeholder = { Text("Запишите основные тезисы...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("note_content_input")
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Теги (через запятую)") },
                    placeholder = { Text("работа, идеи, проект") },
                    modifier = Modifier.fillMaxWidth().testTag("note_tags_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onConfirm(title.ifBlank { "Без названия" }, content, tags)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgentEmerald, contentColor = AgentVoidDark),
                enabled = title.isNotBlank() || content.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_note_button")
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = AgentTextSecondary)
            }
        },
        containerColor = if (isDarkTheme) AgentSurfaceDark else Color.White
    )
}
