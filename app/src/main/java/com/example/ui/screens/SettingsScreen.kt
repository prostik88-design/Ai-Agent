package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.service.BatteryState
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modelProvider: String,
    apiKey: String,
    selectedLanguage: String,
    isDarkTheme: Boolean,
    modelTemperature: Float,
    firebaseUser: FirebaseUser?,
    batteryState: BatteryState,
    useLocalModelOnly: Boolean,
    isTtsEnabled: Boolean,
    telegramBotToken: String,
    telegramChatId: String,
    onModelProviderChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onSignOutFirebase: () -> Unit,
    onToggleLocalModel: (Boolean) -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onToggleSimulatedLowPower: (Boolean) -> Unit,
    onSaveTelegramCredentials: (String, String) -> Unit,
    onSendTelegramTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }
    var isAgentSubmenuExpanded by remember { mutableStateOf(false) }

    var apiKeyInput by remember { mutableStateOf(apiKey) }
    var tgTokenInput by remember { mutableStateOf(telegramBotToken) }
    var tgChatIdInput by remember { mutableStateOf(telegramChatId) }

    val languages = listOf("Русский", "English", "Español", "Français", "Українська")

    // Google Sign-In Launcher via Credential Manager
    fun launchGoogleSignIn() {
        coroutineScope.launch {
            isSigningIn = true
            signInError = null
            try {
                val serverClientId = context.getString(R.string.default_web_client_id)
                val googleSignInOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleSignInOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(
                    request = request,
                    context = context as Activity
                )

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential)
            } catch (e: GetCredentialCancellationException) {
                signInError = "Вход отменен пользователем."
            } catch (e: Exception) {
                signInError = "Ошибка входа: ${e.localizedMessage ?: "Не удалось авторизоваться"}"
            } finally {
                isSigningIn = false
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AgentVoidDark else Color(0xFFF1F5F9))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp)
    ) {
        // Section: Header
        item {
            Text(
                text = "НАСТРОЙКИ СИСТЕМЫ & ИИ МОДЕЛИ",
                color = if (isDarkTheme) AgentCyan else Color(0xFF0284C7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Section 1: Model Provider Selection
        item {
            Surface(
                color = if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. ПРОВАЙДЕР МОДЕЛИ И АВТОРИЗАЦИЯ",
                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onModelProviderChange("API_KEY") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (modelProvider == "API_KEY") AgentCyan else (if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)),
                                contentColor = if (modelProvider == "API_KEY") AgentVoidDark else (if (isDarkTheme) AgentTextSecondary else Color(0xFF334155))
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("provider_api_key")
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("API Ключ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onModelProviderChange("GOOGLE_ACCOUNT") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (modelProvider == "GOOGLE_ACCOUNT") Color(0xFF4285F4) else (if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)),
                                contentColor = if (modelProvider == "GOOGLE_ACCOUNT") Color.White else (if (isDarkTheme) AgentTextSecondary else Color(0xFF334155))
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("provider_google_account")
                        ) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (modelProvider == "API_KEY") {
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                onApiKeyChange(it)
                            },
                            label = { Text("API Ключ (Gemini / Custom Provider)") },
                            placeholder = { Text("AIzaSy...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                focusedBorderColor = AgentCyan,
                                unfocusedBorderColor = AgentCardStrokeDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                        )
                    } else {
                        // Google Account View
                        if (firebaseUser != null) {
                            Surface(
                                color = Color(0xFF4285F4).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4285F4)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (firebaseUser.displayName?.firstOrNull() ?: firebaseUser.email?.firstOrNull() ?: 'U').uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = firebaseUser.displayName ?: "Google Пользователь",
                                                color = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = firebaseUser.email ?: "",
                                                color = if (isDarkTheme) AgentTextSecondary else Color(0xFF64748B),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onSignOutFirebase,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AgentRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AgentRed.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Выйти", fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { launchGoogleSignIn() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4285F4),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("google_sign_in_button"),
                                enabled = !isSigningIn
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Войти через Google Account (Firebase)", fontWeight = FontWeight.Bold)
                            }

                            if (signInError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = signInError ?: "", color = AgentRed, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Interface Language
        item {
            Surface(
                color = if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. ЯЗЫК ИНТЕРФЕЙСА (LANGUAGE)",
                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            Surface(
                                color = if (isSelected) AgentCyan else (if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clickable { onLanguageChange(lang) }
                                    .border(1.dp, if (isSelected) AgentCyan else (if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1)), RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = lang,
                                    color = if (isSelected) AgentVoidDark else (if (isDarkTheme) AgentTextPrimary else Color(0xFF1E293B)),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Interface Theme (Dark vs Light)
        item {
            Surface(
                color = if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. ТЕМА ОФОРМЛЕНИЯ",
                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onThemeChange(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) AgentCyan else (if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)),
                                contentColor = if (isDarkTheme) AgentVoidDark else (if (isDarkTheme) AgentTextSecondary else Color(0xFF334155))
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("theme_dark_button")
                        ) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Темная тема", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onThemeChange(false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDarkTheme) AgentCyan else (if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)),
                                contentColor = if (!isDarkTheme) AgentVoidDark else (if (isDarkTheme) AgentTextSecondary else Color(0xFF334155))
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("theme_light_button")
                        ) {
                            Icon(imageVector = Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Светлая тема", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 4: Model Temperature
        item {
            Surface(
                color = if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isDarkTheme) AgentCardStrokeDark else Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. ТЕМПЕРАТУРА ОТВЕТОВ МОДЕЛИ",
                            color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            color = AgentAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = String.format("%.2f", modelTemperature),
                                color = AgentAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (modelTemperature < 0.3f) "Строгие, детерминированные и аналитические ответы"
                        else if (modelTemperature < 0.7f) "Сбалансированный режим (точность + гибкость)"
                        else "Высокая креативность и вариативность идей",
                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF64748B),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = modelTemperature,
                        onValueChange = onTemperatureChange,
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = AgentAmber,
                            activeTrackColor = AgentAmber,
                            inactiveTrackColor = if (isDarkTheme) AgentSurfaceVariantDark else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.testTag("temperature_slider")
                    )
                }
            }
        }

        // Section 5: Submenu 'Настройки агента'
        item {
            Surface(
                color = if (isDarkTheme) AgentSurfaceDark else Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AgentCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAgentSubmenuExpanded = !isAgentSubmenuExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = AgentCyan, modifier = Modifier.size(20.dp))
                            Text(
                                text = "ПОДМЕНЮ: НАСТРОЙКИ АГЕНТА",
                                color = AgentCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Icon(
                            imageVector = if (isAgentSubmenuExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = if (isDarkTheme) AgentTextSecondary else Color(0xFF64748B)
                        )
                    }

                    AnimatedVisibility(visible = isAgentSubmenuExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 100% Local AI Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "100% Локальный ИИ (Zero Telemetry)",
                                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Все данные обрабатываются исключительно локально на устройстве",
                                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = useLocalModelOnly,
                                    onCheckedChange = onToggleLocalModel,
                                    colors = SwitchDefaults.colors(checkedThumbColor = AgentVoidDark, checkedTrackColor = AgentCyan)
                                )
                            }

                            // TTS Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Озвучивание ответов (TTS)",
                                        color = if (isDarkTheme) AgentTextPrimary else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Голосовой ответ после выполнения команды",
                                        color = if (isDarkTheme) AgentTextSecondary else Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                                Switch(
                                    checked = isTtsEnabled,
                                    onCheckedChange = onToggleTts,
                                    colors = SwitchDefaults.colors(checkedThumbColor = AgentVoidDark, checkedTrackColor = AgentAmber)
                                )
                            }

                            // Telegram credentials
                            Divider(color = AgentCardStrokeDark, thickness = 1.dp)

                            Text(
                                text = "TELEGRAM & МЕССЕНДЖЕРЫ",
                                color = Color(0xFF29B6F6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            OutlinedTextField(
                                value = tgTokenInput,
                                onValueChange = {
                                    tgTokenInput = it
                                    onSaveTelegramCredentials(tgTokenInput, tgChatIdInput)
                                },
                                label = { Text("Telegram Bot Token") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                    focusedBorderColor = AgentCyan,
                                    unfocusedBorderColor = AgentCardStrokeDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tgChatIdInput,
                                onValueChange = {
                                    tgChatIdInput = it
                                    onSaveTelegramCredentials(tgTokenInput, tgChatIdInput)
                                },
                                label = { Text("Telegram Chat ID") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) AgentTextPrimary else Color.Black,
                                    focusedBorderColor = AgentCyan,
                                    unfocusedBorderColor = AgentCardStrokeDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = onSendTelegramTest,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Отправить тестовый сигнал в Telegram", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Divider(color = AgentCardStrokeDark, thickness = 1.dp)

                            // Battery Telemetry Card inside Agent Settings
                            BatteryTelemetryCard(
                                batteryState = batteryState,
                                onToggleSimulatedLowPower = onToggleSimulatedLowPower
                            )
                        }
                    }
                }
            }
        }
    }
}
