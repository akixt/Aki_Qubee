package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class AudioPlaybackState(
    val isTtsReady: Boolean = false,
    val isOromoVoiceSupported: Boolean = false,
    val isSpeaking: Boolean = false,
    val currentText: String? = null,
    val isRecording: Boolean = false,
    val hasRecordedAudio: Boolean = false,
    val isPlayingRecording: Boolean = false,
    val isPlayingAiModel: Boolean = false,
    val repeatCount: Int = 1,
    val errorMessage: String? = null
)

class AudioLearningManager(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var recorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var aiAudioTrack: AudioTrack? = null

    private val recordedFile: File = File(appContext.cacheDir, "aki_qubee_pronunciation.m4a")

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    init {
        runCatching {
            tts = TextToSpeech(appContext, this).apply {
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.value = _state.value.copy(isSpeaking = true, errorMessage = null)
                    }

                    override fun onDone(utteranceId: String?) {
                        _state.value = _state.value.copy(isSpeaking = false)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = _state.value.copy(
                            isSpeaking = false,
                            errorMessage = "Device speech engine encountered an error."
                        )
                    }
                })
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val oromoLocale = Locale.Builder().setLanguage("om").build()
            val result = tts?.setLanguage(oromoLocale) ?: TextToSpeech.LANG_MISSING_DATA
            val hasOromo = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!hasOromo) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setSpeechRate(0.88f)
            _state.value = _state.value.copy(
                isTtsReady = true,
                isOromoVoiceSupported = hasOromo
            )
        } else {
            _state.value = _state.value.copy(isTtsReady = false, isOromoVoiceSupported = false)
        }
    }

    fun setRepeatCount(count: Int) {
        _state.value = _state.value.copy(repeatCount = count.coerceIn(1, 3))
    }

    fun speak(text: String, speechRate: Float = 0.88f, repeatOverride: Int? = null): Boolean {
        val engine = tts ?: return false
        if (!_state.value.isTtsReady || text.isBlank()) return false
        stopAllPlayback()
        engine.setSpeechRate(speechRate.coerceIn(0.6f, 1.3f))
        val repeats = (repeatOverride ?: _state.value.repeatCount).coerceIn(1, 3)
        _state.value = _state.value.copy(isSpeaking = true, currentText = text, errorMessage = null)
        for (i in 0 until repeats) {
            val queueMode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(text, queueMode, Bundle(), "aki_${text.hashCode()}_$i")
            if (i < repeats - 1) {
                engine.playSilentUtterance(280L, TextToSpeech.QUEUE_ADD, "pause_$i")
            }
        }
        return true
    }

    fun stopSpeaking() {
        runCatching { tts?.stop() }
        _state.value = _state.value.copy(isSpeaking = false)
    }

    fun startRecording(): Boolean {
        stopAllPlayback()
        return try {
            if (recordedFile.exists()) recordedFile.delete()
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(appContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96_000)
                setAudioSamplingRate(24_000)
                setOutputFile(recordedFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            _state.value = _state.value.copy(
                isRecording = true,
                hasRecordedAudio = false,
                errorMessage = null
            )
            true
        } catch (e: Exception) {
            Log.e("AudioLearningManager", "Failed to start recording", e)
            runCatching { recorder?.release() }
            recorder = null
            _state.value = _state.value.copy(
                isRecording = false,
                errorMessage = e.localizedMessage ?: "Unable to access microphone."
            )
            false
        }
    }

    fun stopRecording(): File? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            val valid = recordedFile.exists() && recordedFile.length() > 0L
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = valid
            )
            if (valid) recordedFile else null
        } catch (e: Exception) {
            Log.w("AudioLearningManager", "Error stopping recording", e)
            runCatching { recorder?.release() }
            recorder = null
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = false,
                errorMessage = "Recording was too short. Please hold or speak for a moment."
            )
            null
        }
    }

    suspend fun readRecordedAudioBytes(): ByteArray? = withContext(Dispatchers.IO) {
        if (recordedFile.exists() && recordedFile.length() > 0L) {
            recordedFile.readBytes()
        } else {
            null
        }
    }

    fun playRecordedAudio() {
        if (!recordedFile.exists() || recordedFile.length() == 0L) return
        stopAllPlayback()
        try {
            val player = MediaPlayer().apply {
                setDataSource(recordedFile.absolutePath)
                setOnCompletionListener {
                    _state.value = _state.value.copy(isPlayingRecording = false)
                }
                setOnErrorListener { _, _, _ ->
                    _state.value = _state.value.copy(
                        isPlayingRecording = false,
                        errorMessage = "Could not play recorded audio."
                    )
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = player
            _state.value = _state.value.copy(isPlayingRecording = true, errorMessage = null)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isPlayingRecording = false,
                errorMessage = e.localizedMessage ?: "Failed to play recording."
            )
        }
    }

    suspend fun playAiPcmAudio(pcmBytes: ByteArray, speed: Float = 1.0f) {
        if (pcmBytes.isEmpty()) return
        stopAllPlayback()
        withContext(Dispatchers.IO) {
            try {
                val sampleRate = (24_000 * speed.coerceIn(0.7f, 1.3f)).toInt()
                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(pcmBytes.size)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(maxOf(minBuffer, pcmBytes.size))
                    .build()

                aiAudioTrack = track
                _state.value = _state.value.copy(isPlayingAiModel = true, errorMessage = null)
                track.write(pcmBytes, 0, pcmBytes.size)
                track.play()
                val durationMs = ((pcmBytes.size / 2.0) / sampleRate * 1000.0).toLong() + 150L
                kotlinx.coroutines.delay(durationMs)
                runCatching {
                    track.stop()
                    track.release()
                }
                aiAudioTrack = null
                _state.value = _state.value.copy(isPlayingAiModel = false)
            } catch (e: Exception) {
                Log.w("AudioLearningManager", "AudioTrack playback failed", e)
                _state.value = _state.value.copy(
                    isPlayingAiModel = false,
                    errorMessage = "Audio playback failed."
                )
            }
        }
    }

    fun clearRecording() {
        stopAllPlayback()
        if (recordedFile.exists()) recordedFile.delete()
        _state.value = _state.value.copy(hasRecordedAudio = false, errorMessage = null)
    }

    fun stopAllPlayback() {
        stopSpeaking()
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        runCatching {
            aiAudioTrack?.stop()
            aiAudioTrack?.release()
        }
        aiAudioTrack = null
        _state.value = _state.value.copy(
            isPlayingRecording = false,
            isPlayingAiModel = false
        )
    }

    fun release() {
        stopAllPlayback()
        runCatching { recorder?.release() }
        recorder = null
        runCatching { tts?.shutdown() }
        tts = null
    }
}
