package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutomationWorkflowEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.WorkflowExecutionLogEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    workflows: List<AutomationWorkflowEntity> = emptyList(),
    workflowLogs: List<WorkflowExecutionLogEntity> = emptyList(),
    isDarkTheme: Boolean,
    onCreateProject: (title: String, desc: String, category: String, tags: String) -> Unit,
    onUpdateProgress: (ProjectEntity, Int) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onToggleWorkflow: (Long, Boolean) -> Unit = { _, _ -> },
    onExecuteWorkflow: (AutomationWorkflowEntity) -> Unit = {},
    onCreateWorkflow: (title: String, desc: String, category: String, triggerType: String, triggerValue: String, actionsSummary: String) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteWorkflow: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Workflows, 1: Projects
    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var showCreateWorkflowDialog by remember { mutableStateOf(false) }
    var selectedWorkflowForHistory by remember { mutableStateOf<AutomationWorkflowEntity?>(null) }

    val activeWorkflowsCount = workflows.count { it.isEnabled }
    val completedProjectsCount = projects.count { it.status == "COMPLETED" || it.progressPercent >= 100 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AgentVoidDark else Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Sub-tabs: Workflows (Pipelines) vs Projects
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (selectedSubTab == 0) AgentCyan.copy(alpha = 0.2f) else if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedSubTab = 0 }
                    .border(
                        1.dp,
                        if (selectedSubTab == 0) AgentCyan else if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (selectedSubTab == 0) AgentCyan else AgentTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ПАЙПЛАЙНЫ ($activeWorkflowsCount/${workflows.size})",
                        color = if (selectedSubTab == 0) AgentCyan else if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Surface(
                color = if (selectedSubTab == 1) AgentCyan.copy(alpha = 0.2f) else if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedSubTab = 1 }
                    .border(
                        1.dp,
                        if (selectedSubTab == 1) AgentCyan else if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (selectedSubTab == 1) AgentCyan else AgentTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ПРОЕКТЫ (${projects.size})",
                        color = if (selectedSubTab == 1) AgentCyan else if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // TAB 0: AUTOMATION WORKFLOWS & ROUTINES
        if (selectedSubTab == 0) {
            // Header for Workflows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ЦЕПОЧКИ АВТОМАТИЗАЦИИ",
                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Оркестрация календарей, заметок и внешних MCP сервисов",
                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showCreateWorkflowDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AgentCyan, contentColor = AgentVoidDark),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp).testTag("create_workflow_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Создать рутину", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Secondary Power Optimizer Info Pill
            Surface(
                color = AgentSurfaceDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, AgentEmerald.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = AgentEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Фоновый оптимизатор питания активен: безопасный контроль при разряде аккумулятора",
                        color = AgentEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (workflows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.AutoMode, contentDescription = null, tint = AgentTextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Нет активных цепочек автоматизации", color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569), fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 48.dp)
                ) {
                    items(workflows, key = { it.id }) { workflow ->
                        WorkflowCard(
                            workflow = workflow,
                            isDarkTheme = isDarkTheme,
                            onToggle = { enabled -> onToggleWorkflow(workflow.id, enabled) },
                            onExecute = { onExecuteWorkflow(workflow) },
                            onShowHistory = { selectedWorkflowForHistory = workflow },
                            onDelete = { onDeleteWorkflow(workflow.id) }
                        )
                    }
                }
            }
        }

        // TAB 1: PROJECTS & TASKS
        else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "РАБОЧИЕ ПРОЕКТЫ",
                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Всего: ${projects.size} • Завершено: $completedProjectsCount",
                        color = if (isDarkTheme) AgentCyan else Color(0xFF0284C7),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showCreateProjectDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AgentCyan, contentColor = AgentVoidDark),
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
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = AgentTextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Проектов пока нет", color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Создайте проект для структурирования задач и MCP инструментов", color = AgentTextTertiary, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 48.dp)
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
    }

    // Dialog: Create Project
    if (showCreateProjectDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateProjectDialog = false },
            onConfirm = { title, desc, cat, tags ->
                onCreateProject(title, desc, cat, tags)
                showCreateProjectDialog = false
            }
        )
    }

    // Dialog: Create Automation Workflow
    if (showCreateWorkflowDialog) {
        CreateWorkflowDialog(
            onDismiss = { showCreateWorkflowDialog = false },
            onConfirm = { title, desc, category, triggerType, triggerValue, actionsSummary ->
                onCreateWorkflow(title, desc, category, triggerType, triggerValue, actionsSummary)
                showCreateWorkflowDialog = false
            }
        )
    }

    // Modal: Workflow Execution History
    selectedWorkflowForHistory?.let { workflow ->
        val relevantLogs = workflowLogs.filter { it.workflowId == workflow.id || it.workflowTitle == workflow.title }
        WorkflowHistoryDialog(
            workflow = workflow,
            logs = relevantLogs,
            onDismiss = { selectedWorkflowForHistory = null }
        )
    }
}

// Workflow Execution History Dialog

// Workflow Execution History Dialog
@Composable
fun WorkflowHistoryDialog(
    workflow: AutomationWorkflowEntity,
    logs: List<WorkflowExecutionLogEntity>,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgentSurfaceDark,
        title = {
            Column {
                Text(
                    text = "ИСТОРИЯ ВЫПОЛНЕНИЯ",
                    color = AgentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = workflow.title,
                    color = AgentTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Логи выполнения пока отсутствуют. Нажмите «Запустить», чтобы инициировать сценарий.",
                            color = AgentTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            Surface(
                                color = AgentVoidDark,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = if (log.status == "SUCCESS") AgentEmerald.copy(alpha = 0.2f) else AgentAmber.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (log.status == "SUCCESS") "✅ УСПЕШНО" else "⚠️ ЩАДЯЩИЙ РЕЖИМ",
                                                color = if (log.status == "SUCCESS") AgentEmerald else AgentAmber,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }

                                        Text(
                                            text = "⏱️ ${log.durationMs} мс",
                                            color = AgentCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.summary,
                                        color = AgentTextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )

                                    if (log.batteryStateInfo.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🔋 ${log.batteryStateInfo}",
                                            color = AgentTextTertiary,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateFormat.format(Date(log.executedAtMillis)),
                                        color = AgentTextTertiary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AgentCyan, contentColor = AgentVoidDark)
            ) {
                Text("Закрыть")
            }
        }
    )
}

// Dialog: Create Automation Workflow
@Composable
fun CreateWorkflowDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, category: String, triggerType: String, triggerValue: String, actionsSummary: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ROUTINE") }
    var triggerValue by remember { mutableStateOf("Ежедневно в 09:00") }
    var actionsSummary by remember { mutableStateOf("Календарь ➔ Заметки ➔ Telegram") }

    val triggerPresets = listOf(
        "Ежедневно в 09:00",
        "За 15 мин до встречи",
        "Каждые 3 часа",
        "Заряд батареи <= 20%",
        "По голосовой команде"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgentSurfaceDark,
        title = {
            Text(text = "Создать цепочку автоматизации", color = AgentCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название процесса") },
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
                    label = { Text("Описание и цели сценария") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Выберите шаблон триггера:",
                    color = AgentTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    triggerPresets.forEach { preset ->
                        Surface(
                            color = if (triggerValue == preset) AgentCyan.copy(alpha = 0.25f) else AgentVoidDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    triggerValue = preset
                                    if (preset.contains("Заряд")) category = "OPTIMIZER"
                                    else if (preset.contains("встреч")) category = "CALENDAR"
                                }
                                .border(1.dp, if (triggerValue == preset) AgentCyan else AgentCardStrokeDark, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = preset,
                                color = if (triggerValue == preset) AgentCyan else AgentTextSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = actionsSummary,
                    onValueChange = { actionsSummary = it },
                    label = { Text("Цепочка шагов (через ➔)") },
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
                        val triggerType = when {
                            triggerValue.contains("Заряд") -> "BATTERY_OPTIMIZER"
                            triggerValue.contains("встреч") -> "CALENDAR_EVENT"
                            triggerValue.contains("команд") -> "VOICE_COMMAND"
                            else -> "TIME_SCHEDULE"
                        }
                        onConfirm(title, desc, category, triggerType, triggerValue, actionsSummary)
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

// Existing Project Card
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
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("+15% прогресс", fontSize = 10.sp, color = AgentEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { project.progressPercent / 100f },
                color = accentColor,
                trackColor = if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tasks and Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = AgentTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Задачи: ${project.completedTaskCount}/${project.taskCount}",
                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF475569),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Дедлайн: ${dateFormat.format(Date(project.deadlineMillis))}",
                    color = AgentTextTertiary,
                    fontSize = 10.sp
                )
            }

            if (project.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    project.tags.split(",").forEach { tag ->
                        Surface(
                            color = if (isDarkTheme) AgentVoidDark else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#${tag.trim()}",
                                color = if (isDarkTheme) AgentTextTertiary else Color(0xFF64748B),
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Create Project Dialog
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
