package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectChatMessageEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    messages: List<DirectChatMessageEntity>,
    isProcessing: Boolean,
    isDarkTheme: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isProcessing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "💡 Предложи 3 идеи для стартапа",
        "📝 Напиши структуру делового отчета",
        "🎯 Объясни квантовые вычисления простыми словами",
        "🔍 Как оптимизировать время работы команды?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AgentVoidDark else Color(0xFFF1F5F9))
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ДИАЛОГОВЫЙ ЧАТ (DIRECT AI BOT)",
                    color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Чистый диалог без агентских цепочек и MCP",
                    color = if (isDarkTheme) AgentTextSecondary else Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(30.dp).testTag("clear_direct_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Очистить чат",
                    tint = AgentTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role.equals("USER", ignoreCase = true)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = if (isUser) "ВЫ • ${timeFormat.format(Date(msg.timestamp))}" else "ЧАТ-БОТ • ${timeFormat.format(Date(msg.timestamp))}",
                        color = AgentTextTertiary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
                    )

                    Surface(
                        color = if (isUser) {
                            if (isDarkTheme) AgentCyanDark.copy(alpha = 0.35f) else Color(0xFFE0F2FE)
                        } else {
                            if (isDarkTheme) AgentSurfaceDark else Color.White
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .border(
                                width = 1.dp,
                                color = if (isUser) AgentCyan.copy(alpha = 0.5f) else (if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1)),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                )
                            )
                    ) {
                        Text(
                            text = msg.content,
                            color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (isProcessing) {
                item {
                    Surface(
                        color = if (isDarkTheme) AgentSurfaceDark else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, AgentCardStrokeDark, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AgentCyan, strokeWidth = 2.dp)
                            Text(text = "Чат-бот печатает ответ...", fontSize = 12.sp, color = AgentTextSecondary)
                        }
                    }
                }
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Surface(
                    color = if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .clickable { onSendMessage(prompt) }
                        .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = prompt,
                        color = if (isDarkTheme) AgentCyan else Color(0xFF0284C7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        Surface(
            color = if (isDarkTheme) AgentSurfaceDark else Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Напишите сообщение чат-боту...",
                            color = AgentTextTertiary,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("direct_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1),
                        focusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                        cursorColor = AgentCyan,
                        focusedContainerColor = if (isDarkTheme) AgentVoidDark else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDarkTheme) AgentVoidDark else Color(0xFFF8FAFC)
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) AgentCyan else (if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)))
                        .testTag("direct_chat_send_button"),
                    enabled = inputText.isNotBlank() && !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Отправить",
                        tint = if (inputText.isNotBlank()) AgentVoidDark else AgentTextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
