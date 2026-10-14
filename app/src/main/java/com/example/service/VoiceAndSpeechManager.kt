package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAndSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    private var onSpeechResultCallback: ((String) -> Unit)? = null

    init {
        try {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            // Text to speech init fallback
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("ru", "RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.US)
            }
            isTtsReady = true
        }
    }

    fun speak(text: String, speed: Float = 1.0f) {
        if (!isTtsReady || textToSpeech == null) return
        textToSpeech?.setSpeechRate(speed)
        // Clean markdown symbols for cleaner vocalization
        val cleanText = text
            .replace(Regex("[#*`_~]"), "")
            .replace("💭 Мысль:", "Думаю:")
            .replace("⚙️ Вызов MCP:", "Выполняю инструмент:")
            .replace("🎯 Итоговый ответ:", "Результат:")
            .take(350) // Vocalize concise summary
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "HERMES_VOICE_ID")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun startListening(onResult: (String) -> Unit) {
        onSpeechResultCallback = onResult
        _speechError.value = null

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _speechError.value = "Распознавание речи недоступно на данном устройстве. Используйте клавиатуру или тестовые команды."
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2) / 12f).coerceIn(0f, 1f)
                    _soundLevel.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _soundLevel.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _soundLevel.value = 0f
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Не удалось распознать речь"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Время ожидания речи истекло"
                        SpeechRecognizer.ERROR_AUDIO -> "Ошибка аудио"
                        SpeechRecognizer.ERROR_CLIENT -> "Ошибка клиента"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Требуется разрешение на запись звука"
                        else -> "Ошибка распознавания ($error)"
                    }
                    _speechError.value = errorMessage
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _soundLevel.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        onSpeechResultCallback?.invoke(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _speechError.value = "Ошибка запуска микрофона: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignored
        } finally {
            _isListening.value = false
            _soundLevel.value = 0f
        }
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            // Cleanup
        }
    }
}
