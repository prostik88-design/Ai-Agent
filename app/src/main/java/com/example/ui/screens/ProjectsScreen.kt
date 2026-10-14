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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    isDarkTheme: Boolean,
    onCreateProject: (title: String, desc: String, category: String, tags: String) -> Unit,
    onUpdateProgress: (ProjectEntity, Int) -> Unit,
    onDeleteProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val completedCount = projects.count { it.status == "COMPLETED" || it.progressPercent >= 100 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AgentVoidDark else Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ПРОЕКТЫ & РАБОЧИЕ ПАЙПЛАЙНЫ",
                    color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Всего: ${projects.size} • Завершено: $completedCount",
                    color = if (isDarkTheme) AgentCyan else Color(0xFF0284C7),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgentCyan,
                    contentColor = AgentVoidDark
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).testTag("create_project_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Новый проект", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = AgentTextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Проектов пока нет",
                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Создайте проект для структурирования задач и MCP инструментов",
                        color = AgentTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        isDarkTheme = isDarkTheme,
                        onUpdateProgress = { newProgress -> onUpdateProgress(project, newProgress) },
                        onDelete = { onDeleteProject(project.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, desc, cat, tags ->
                onCreateProject(title, desc, cat, tags)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    isDarkTheme: Boolean,
    onUpdateProgress: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = project.progressPercent >= 100 || project.status == "COMPLETED"
    val accentColor = if (isCompleted) AgentEmerald else AgentCyan
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Surface(
        color = if (isDarkTheme) AgentSurfaceDark else Color.White,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isDarkTheme) accentColor.copy(alpha = 0.4f) else Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = project.category,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = if (isCompleted) AgentEmerald.copy(alpha = 0.15f) else AgentAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "ЗАВЕРШЕН" else "В РАБОТЕ",
                            color = if (isCompleted) AgentEmerald else AgentAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp).testTag("delete_project_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить проект",
                        tint = AgentRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.title,
                color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (project.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar and Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Прогресс: ${project.progressPercent}%",
                    color = if (isDarkTheme) AgentTextPrimary else Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                if (!isCompleted) {
                    TextButton(
                        onClick = { onUpdateProgress((project.progressPercent + 15).coerceAtMost(100)) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = AgentEmerald)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+15% этап", fontSize = 11.sp, color = AgentEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${project.tags}",
                    color = AgentCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Дедлайн: ${dateFormat.format(Date(project.deadlineMillis))}",
                    color = AgentTextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Разработка") }
    var tags by remember { mutableStateOf("Автоматизация,MCP") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgentSurfaceDark,
        title = {
            Text(text = "Создать новый проект", color = AgentCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название проекта") },
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
                    label = { Text("Описание и цели") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория (Разработка, Маркетинг, Исследования)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Теги (через запятую)") },
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
                        onConfirm(title, desc, category, tags)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgentCyan, contentColor = AgentVoidDark)
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = AgentTextSecondary)
            }
        }
    )
}
