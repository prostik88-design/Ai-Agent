package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.AgentMessageEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentChatScreen(
    messages: List<AgentMessageEntity>,
    isProcessing: Boolean,
    isListening: Boolean,
    soundLevel: Float,
    speechError: String?,
    onSendMessage: (String, Boolean) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSpeakText: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size, isProcessing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "📅 Брифинг на день",
        "🎙️ Запланируй встречу в Google Calendar завтра в 15:00",
        "📆 Созвон в Outlook в 16:30",
        "📝 Запиши заметку: согласовать договор с клиентом",
        "🔍 Найди информацию о стандарте MCP",
        "✉️ Напиши черновик письма директору",
        "✈️ Отправь отчет в Telegram"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgentVoidDark)
    ) {
        // Chat History Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ДИАЛОГ & АВТОНОМНЫЕ ЗАДАЧИ",
                color = AgentTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
            )
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(28.dp).testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Очистить чат",
                    tint = AgentTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Speech error alert if any
        AnimatedVisibility(visible = speechError != null) {
            Surface(
                color = AgentRed.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, AgentRed, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AgentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = speechError ?: "",
                        color = AgentTextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    message = msg,
                    onSpeak = { onSpeakText(msg.content) }
                )
            }

            if (isProcessing) {
                item {
                    ProcessingIndicatorBubble()
                }
            }
        }

        // Quick Action Prompt Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Surface(
                    color = AgentSurfaceVariantDark,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clickable {
                            onSendMessage(prompt, false)
                        }
                        .border(1.dp, AgentCardStrokeDark, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = prompt,
                        color = AgentCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        Surface(
            color = AgentSurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .border(1.dp, AgentCardStrokeDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mic Button
                VoiceMicOrb(
                    isListening = isListening,
                    soundLevel = soundLevel,
                    onClick = {
                        if (isListening) onStopVoice() else onStartVoice()
                    }
                )

                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isListening) "Слушаю вас..." else "Команда агенту (голос/текст)...",
                            color = if (isListening) AgentCyan else AgentTextTertiary,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AgentCyan,
                        unfocusedBorderColor = AgentCardStrokeDark,
                        focusedTextColor = AgentTextPrimary,
                        unfocusedTextColor = AgentTextPrimary,
                        cursorColor = AgentCyan,
                        focusedContainerColor = AgentVoidDark,
                        unfocusedContainerColor = AgentVoidDark
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText, false)
                            inputText = ""
                        }
                    })
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText, false)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) AgentCyan else AgentSurfaceVariantDark)
                        .testTag("chat_send_button"),
                    enabled = inputText.isNotBlank() && !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Отправить",
                        tint = if (inputText.isNotBlank()) AgentVoidDark else AgentTextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: AgentMessageEntity,
    onSpeak: () -> Unit
) {
    val isUser = message.role.equals("USER", ignoreCase = true)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Label & Timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            if (isUser) {
                if (message.isVoice) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Голосовая команда",
                        tint = AgentCyan,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Text(
                    text = if (message.isVoice) "ВЫ (ГОЛОС)" else "ВЫ",
                    color = AgentTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AgentCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "HERMES AGENT",
                    color = AgentCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "• ${timeFormat.format(Date(message.timestamp))}",
                color = AgentTextTertiary,
                fontSize = 10.sp
            )
        }

        // Message Surface
        Surface(
            color = if (isUser) AgentCyanDark.copy(alpha = 0.25f) else AgentSurfaceDark,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 340.dp)
                .border(
                    width = 1.dp,
                    color = if (isUser) AgentCyan.copy(alpha = 0.5f) else AgentCardStrokeDark,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // If assistant executed a tool
                if (!isUser && message.toolCallsJson.isNotBlank()) {
                    Surface(
                        color = AgentSurfaceVariantDark,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(1.dp, AgentAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = null,
                                tint = AgentAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MCP Tool: ${message.toolCallsJson}",
                                color = AgentAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Hermes Chain of thought Accordion
                if (!isUser && message.reasoningTrace.isNotBlank()) {
                    HermesReasoningAccordion(
                        reasoningTrace = message.reasoningTrace,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Message Text
                Text(
                    text = message.content,
                    color = AgentTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Assistant Audio Speak Action
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            color = AgentVoidDark,
                            shape = CircleShape,
                            modifier = Modifier
                                .clickable { onSpeak() }
                                .border(1.dp, AgentCardStrokeDark, CircleShape)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Озвучить",
                                    tint = AgentCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Озвучить",
                                    color = AgentCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingIndicatorBubble() {
    Surface(
        color = AgentSurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .border(1.dp, AgentAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .widthIn(max = 280.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = AgentAmber,
                strokeWidth = 2.dp
            )
            Column {
                Text(
                    text = "Hermes Agent думает...",
                    color = AgentAmber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Локальный NLP пайплайн и вызов MCP",
                    color = AgentTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
