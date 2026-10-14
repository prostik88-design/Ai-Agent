package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SkillEntity
import com.example.ui.theme.*

@Composable
fun SkillsScreen(
    skills: List<SkillEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onToggleSkill: (String, Boolean) -> Unit,
    onInstallSkill: (String) -> Unit,
    onUninstallSkill: (String) -> Unit,
    onAddNewSkill: (String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "ALL" to "Все навыки",
        "CALENDAR" to "Календари",
        "EMAIL" to "Email",
        "NOTES" to "Заметки",
        "SEARCH" to "Поиск",
        "MESSENGER" to "Мессенджеры",
        "MCP" to "MCP Хранилища"
    )

    val filteredSkills = remember(skills, selectedCategory) {
        if (selectedCategory == "ALL") skills
        else skills.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val installedCount = skills.count { it.isInstalled }

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
                    text = "МЕНЕДЖЕР НАВЫКОВ (SKILLS STORE)",
                    color = AgentTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Установлено $installedCount из ${skills.size} модулей",
                    color = AgentCyan,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AgentCyan,
                    contentColor = AgentVoidDark
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_custom_skill_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Свой навык", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                Surface(
                    color = if (isSelected) AgentCyan else AgentSurfaceVariantDark,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable { onCategorySelected(catKey) }
                        .border(
                            1.dp,
                            if (isSelected) AgentCyan else AgentCardStrokeDark,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Text(
                        text = catLabel,
                        color = if (isSelected) AgentVoidDark else AgentTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Skills List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            items(filteredSkills, key = { it.id }) { skill ->
                SkillCard(
                    skill = skill,
                    onToggle = { enabled -> onToggleSkill(skill.id, enabled) },
                    onInstall = { onInstallSkill(skill.id) },
                    onUninstall = { onUninstallSkill(skill.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddSkillDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, cat, triggers ->
                onAddNewSkill(name, desc, cat, triggers)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SkillCard(
    skill: SkillEntity,
    onToggle: (Boolean) -> Unit,
    onInstall: () -> Unit,
    onUninstall: () -> Unit
) {
    val categoryColor = when (skill.category.uppercase()) {
        "CALENDAR" -> AgentCyan
        "EMAIL" -> AgentAmber
        "NOTES" -> AgentEmerald
        "SEARCH" -> AgentViolet
        "MESSENGER" -> Color(0xFF29B6F6)
        else -> AgentCyan
    }

    Surface(
        color = AgentSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (skill.isEnabled && skill.isInstalled) categoryColor.copy(alpha = 0.5f) else AgentCardStrokeDark,
                RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getSkillIcon(skill.icon),
                                contentDescription = null,
                                tint = categoryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = skill.name,
                            color = AgentTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = skill.category,
                                color = categoryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "v${skill.version} • ${skill.author}",
                                color = AgentTextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (skill.isInstalled) {
                    Switch(
                        checked = skill.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AgentVoidDark,
                            checkedTrackColor = categoryColor,
                            uncheckedThumbColor = AgentTextTertiary,
                            uncheckedTrackColor = AgentSurfaceVariantDark
                        ),
                        modifier = Modifier.testTag("toggle_skill_${skill.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = skill.description,
                color = AgentTextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            if (skill.triggerWords.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = AgentAmber,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Триггеры: ${skill.triggerWords}",
                        color = AgentTextTertiary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (skill.isInstalled) {
                        if (skill.isEnabled) AgentEmerald.copy(alpha = 0.15f) else AgentTextTertiary.copy(alpha = 0.15f)
                    } else {
                        AgentAmber.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (skill.isInstalled) {
                            if (skill.isEnabled) "АКТИВЕН" else "ОТКЛЮЧЕН"
                        } else {
                            "ДОСТУПЕН В КАТАЛОГЕ"
                        },
                        color = if (skill.isInstalled) {
                            if (skill.isEnabled) AgentEmerald else AgentTextTertiary
                        } else {
                            AgentAmber
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (skill.isInstalled) {
                    OutlinedButton(
                        onClick = onUninstall,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgentRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AgentRed.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = "Удалить", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgentCyan,
                            contentColor = AgentVoidDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(text = "Установить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getSkillIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "calendar_month" -> Icons.Default.CalendarMonth
        "event_available" -> Icons.Default.EventAvailable
        "mail" -> Icons.Default.Mail
        "edit_note" -> Icons.Default.EditNote
        "travel_explore" -> Icons.Default.TravelExplore
        "send" -> Icons.Default.Send
        "cloud_sync" -> Icons.Default.CloudSync
        "mic" -> Icons.Default.Mic
        else -> Icons.Default.Extension
    }
}

@Composable
fun AddSkillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, category: String, triggers: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PRODUCTIVITY") }
    var triggers by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AgentSurfaceDark,
        title = {
            Text(
                text = "Добавить пользовательский навык",
                color = AgentCyan,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название навыка") },
                    placeholder = { Text("Например: Парсер отчетов") },
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
                    label = { Text("Описание и логика") },
                    placeholder = { Text("Что должен делать навык...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = triggers,
                    onValueChange = { triggers = it },
                    label = { Text("Триггерные фразы (через запятую)") },
                    placeholder = { Text("отчет,сводка,выгрузка") },
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
                    if (name.isNotBlank()) {
                        onConfirm(name, desc.ifBlank { "Пользовательский навык автоматизации" }, category, triggers)
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
