package com.example.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

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

    private val recordedM4aFile: File = File(appContext.cacheDir, "aki_qubee_pronunciation.m4a")
    private val recordedWavFile: File = File(appContext.cacheDir, "aki_qubee_pronunciation.wav")
    private var activeRecordedFile: File = recordedM4aFile
    private var activeRecordedMimeType: String = "audio/mp4"
    private var recordingStartedAtMs: Long = 0L

    // AudioRecord PCM fallback for emulators / devices where MediaRecorder AAC fails
    private var pcmAudioRecord: AudioRecord? = null
    private val isPcmCaptureActive = AtomicBoolean(false)
    private var pcmCaptureThread: Thread? = null
    private val pcmBufferLock = Any()
    private var pcmOutputStream: ByteArrayOutputStream? = null

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
        cleanupActiveRecorder()

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _state.value = _state.value.copy(
                isRecording = false,
                errorMessage = "Microphone permission is required to record voice."
            )
            return false
        }

        if (recordedM4aFile.exists()) recordedM4aFile.delete()
        if (recordedWavFile.exists()) recordedWavFile.delete()

        // Prefer AudioRecord 16kHz PCM WAV or MediaRecorder 16kHz/44.1kHz AAC:
        // On Android emulators, MediaRecorder AAC at 24kHz frequently fails prepare/start.
        val startedMediaRecorder = tryStartMediaRecorder(
            audioSource = MediaRecorder.AudioSource.MIC,
            sampleRate = 16_000
        ) || tryStartMediaRecorder(
            audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate = 44_100
        )

        if (startedMediaRecorder) {
            activeRecordedFile = recordedM4aFile
            activeRecordedMimeType = "audio/mp4"
            recordingStartedAtMs = System.currentTimeMillis()
            _state.value = _state.value.copy(
                isRecording = true,
                hasRecordedAudio = false,
                errorMessage = null
            )
            return true
        }

        // Universal fallback: raw AudioRecord PCM -> WAV (works on virtually all emulators)
        val startedPcmFallback = tryStartPcmRecorder()
        if (startedPcmFallback) {
            activeRecordedFile = recordedWavFile
            activeRecordedMimeType = "audio/wav"
            recordingStartedAtMs = System.currentTimeMillis()
            _state.value = _state.value.copy(
                isRecording = true,
                hasRecordedAudio = false,
                errorMessage = null
            )
            return true
        }

        _state.value = _state.value.copy(
            isRecording = false,
            hasRecordedAudio = false,
            errorMessage = "Microphone could not start on this device/emulator. Enable virtual microphone audio input in emulator settings."
        )
        return false
    }

    private fun tryStartMediaRecorder(audioSource: Int, sampleRate: Int): Boolean {
        var mediaRecorder: MediaRecorder? = null
        return try {
            if (recordedM4aFile.exists()) recordedM4aFile.delete()
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(appContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder.apply {
                setAudioSource(audioSource)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(1)
                setAudioSamplingRate(sampleRate)
                setAudioEncodingBitRate(64_000)
                setOutputFile(recordedM4aFile.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            true
        } catch (e: Exception) {
            Log.w(TAG, "MediaRecorder($audioSource, ${sampleRate}Hz) failed: ${e.message}")
            runCatching { mediaRecorder?.reset() }
            runCatching { mediaRecorder?.release() }
            recorder = null
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun tryStartPcmRecorder(): Boolean {
        val sampleRate = PCM_SAMPLE_RATE_HZ
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) return false
        val bufferSize = maxOf(minBuf * 2, 4096)

        val sources = listOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.DEFAULT
        )

        for (source in sources) {
            var candidate: AudioRecord? = null
            try {
                candidate = AudioRecord(
                    source,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
                if (candidate.state != AudioRecord.STATE_INITIALIZED) {
                    candidate.release()
                    continue
                }
                candidate.startRecording()
                if (candidate.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                    candidate.release()
                    continue
                }

                pcmAudioRecord = candidate
                synchronized(pcmBufferLock) {
                    pcmOutputStream = ByteArrayOutputStream(32_768)
                }
                isPcmCaptureActive.set(true)
                pcmCaptureThread = Thread({
                    val chunk = ByteArray(bufferSize)
                    while (isPcmCaptureActive.get()) {
                        val read = candidate.read(chunk, 0, chunk.size)
                        if (read > 0) {
                            synchronized(pcmBufferLock) {
                                pcmOutputStream?.write(chunk, 0, read)
                            }
                        }
                    }
                }, "AkiQubeePcmCapture").apply { start() }
                return true
            } catch (e: Exception) {
                Log.w(TAG, "AudioRecord fallback($source) failed: ${e.message}")
                runCatching { candidate?.release() }
            }
        }
        return false
    }

    fun stopRecording(): File? {
        if (isPcmCaptureActive.get() || pcmAudioRecord != null) {
            return stopPcmRecording()
        }
        val activeRecorder = recorder ?: return null
        return try {
            val elapsedMs = System.currentTimeMillis() - recordingStartedAtMs
            activeRecorder.stop()
            activeRecorder.release()
            recorder = null
            val valid = recordedM4aFile.exists() && recordedM4aFile.length() > 128L && elapsedMs >= 250L
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = valid,
                errorMessage = if (valid) null else "Waraabiin gabaabbateera — yoo xiqqaate sekondii 1 dubbadhu. / Recording was too short."
            )
            if (valid) recordedM4aFile else null
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping MediaRecorder", e)
            runCatching { activeRecorder.reset() }
            runCatching { activeRecorder.release() }
            recorder = null
            if (recordedM4aFile.exists()) recordedM4aFile.delete()
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = false,
                errorMessage = "Waraabiin gabaabbateera — yoo xiqqaate sekondii 1 dubbadhu. / Recording was too short."
            )
            null
        }
    }

    private fun stopPcmRecording(): File? {
        isPcmCaptureActive.set(false)
        runCatching { pcmAudioRecord?.stop() }
        runCatching { pcmCaptureThread?.join(500L) }
        runCatching { pcmAudioRecord?.release() }
        pcmAudioRecord = null
        pcmCaptureThread = null

        val pcmBytes = synchronized(pcmBufferLock) {
            val data = pcmOutputStream?.toByteArray() ?: ByteArray(0)
            pcmOutputStream = null
            data
        }

        if (pcmBytes.size < 1600) {
            if (recordedWavFile.exists()) recordedWavFile.delete()
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = false,
                errorMessage = "No microphone sound detected or recording too short. Hold and speak clearly."
            )
            return null
        }

        return try {
            val wavBytes = buildWavFileBytes(pcmBytes, sampleRateHz = PCM_SAMPLE_RATE_HZ)
            FileOutputStream(recordedWavFile).use { out ->
                out.write(wavBytes)
            }
            activeRecordedFile = recordedWavFile
            activeRecordedMimeType = "audio/wav"
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = true,
                errorMessage = null
            )
            recordedWavFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write WAV recording", e)
            _state.value = _state.value.copy(
                isRecording = false,
                hasRecordedAudio = false,
                errorMessage = "Could not save recorded audio."
            )
            null
        }
    }

    suspend fun readRecordedAudioBytes(): ByteArray? = withContext(Dispatchers.IO) {
        if (activeRecordedFile.exists() && activeRecordedFile.length() > 0L) {
            activeRecordedFile.readBytes()
        } else {
            null
        }
    }

    suspend fun readRecordedAudioWithMime(): Pair<ByteArray, String>? = withContext(Dispatchers.IO) {
        if (activeRecordedFile.exists() && activeRecordedFile.length() > 0L) {
            activeRecordedFile.readBytes() to activeRecordedMimeType
        } else {
            null
        }
    }

    fun playRecordedAudio() {
        if (!activeRecordedFile.exists() || activeRecordedFile.length() == 0L) return
        stopAllPlayback()
        try {
            val player = MediaPlayer().apply {
                setDataSource(activeRecordedFile.absolutePath)
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
            var track: AudioTrack? = null
            try {
                val sampleRate = (24_000 * speed.coerceIn(0.7f, 1.3f)).toInt()
                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)
                track = AudioTrack.Builder()
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

                if (track.state != AudioTrack.STATE_INITIALIZED) {
                    track.release()
                    _state.value = _state.value.copy(
                        isPlayingAiModel = false,
                        errorMessage = "Audio output device could not initialize."
                    )
                    return@withContext
                }

                aiAudioTrack = track
                _state.value = _state.value.copy(isPlayingAiModel = true, errorMessage = null)
                track.write(pcmBytes, 0, pcmBytes.size)
                track.play()
                val durationMs = ((pcmBytes.size / 2.0) / sampleRate * 1000.0).toLong() + 180L
                kotlinx.coroutines.delay(durationMs)
                runCatching {
                    track.stop()
                    track.release()
                }
                aiAudioTrack = null
                _state.value = _state.value.copy(isPlayingAiModel = false)
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack playback failed", e)
                runCatching { track?.release() }
                aiAudioTrack = null
                _state.value = _state.value.copy(
                    isPlayingAiModel = false,
                    errorMessage = "Audio playback failed."
                )
            }
        }
    }

    fun clearRecording() {
        stopAllPlayback()
        if (recordedM4aFile.exists()) recordedM4aFile.delete()
        if (recordedWavFile.exists()) recordedWavFile.delete()
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

    private fun cleanupActiveRecorder() {
        isPcmCaptureActive.set(false)
        runCatching { pcmAudioRecord?.stop() }
        runCatching { pcmCaptureThread?.join(200L) }
        runCatching { pcmAudioRecord?.release() }
        pcmAudioRecord = null
        pcmCaptureThread = null
        runCatching { recorder?.stop() }
        runCatching { recorder?.reset() }
        runCatching { recorder?.release() }
        recorder = null
    }

    fun release() {
        stopAllPlayback()
        cleanupActiveRecorder()
        runCatching { tts?.shutdown() }
        tts = null
    }

    companion object {
        private const val TAG = "AudioLearningManager"
        const val PCM_SAMPLE_RATE_HZ = 16_000

        fun buildWavFileBytes(pcmData: ByteArray, sampleRateHz: Int = PCM_SAMPLE_RATE_HZ): ByteArray {
            val channels = 1
            val bitsPerSample = 16
            val byteRate = sampleRateHz * channels * bitsPerSample / 8
            val blockAlign = channels * bitsPerSample / 8
            val totalAudioLen = pcmData.size
            val totalDataLen = totalAudioLen + 36

            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray(Charsets.US_ASCII))
                putInt(totalDataLen)
                put("WAVE".toByteArray(Charsets.US_ASCII))
                put("fmt ".toByteArray(Charsets.US_ASCII))
                putInt(16) // Subchunk1Size for PCM
                putShort(1.toShort()) // AudioFormat = 1 (PCM)
                putShort(channels.toShort())
                putInt(sampleRateHz)
                putInt(byteRate)
                putShort(blockAlign.toShort())
                putShort(bitsPerSample.toShort())
                put("data".toByteArray(Charsets.US_ASCII))
                putInt(totalAudioLen)
            }.array()

            return ByteArray(header.size + pcmData.size).also { out ->
                System.arraycopy(header, 0, out, 0, header.size)
                System.arraycopy(pcmData, 0, out, header.size, pcmData.size)
            }
        }
    }
}
