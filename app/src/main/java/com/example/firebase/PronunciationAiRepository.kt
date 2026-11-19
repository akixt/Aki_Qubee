package com.example.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class PronunciationComparisonResult(
    val accuracyScore: Int,
    val scoreLabel: String,
    val feedbackOm: String,
    val feedbackEn: String,
    val phoneticTip: String,
    val strengths: List<String>,
    val improvements: List<String>
)

sealed interface AiModelState<out T> {
    data object Idle : AiModelState<Nothing>
    data object Loading : AiModelState<Nothing>
    data class Success<T>(val data: T) : AiModelState<T>
    data class Error(val message: String, val isOfflineOrAttestation: Boolean = false) : AiModelState<Nothing>
}

@OptIn(PublicPreviewAPI::class)
class PronunciationAiRepository {

    companion object {
        private const val TAG = "PronunciationAiRepo"
        private const val AI_TIMEOUT_MS = 60_000L
        private const val ANALYSIS_MODEL = "gemini-3.8-flash"
        private const val TTS_MODEL = "gemini-3.1-flash-tts-preview"
    }

    private val analysisSchema by lazy {
        Schema.obj(
            mapOf(
                "accuracyScore" to Schema.integer("Pronunciation accuracy score from 0 to 100."),
                "scoreLabel" to Schema.string("Short rating such as Excellent, Good, Keep Practicing, or Try Again."),
                "feedbackEn" to Schema.string("Constructive pronunciation feedback in clear English."),
                "feedbackOm" to Schema.string("Constructive pronunciation feedback in natural Afaan Oromoo."),
                "phoneticTip" to Schema.string("Clear syllable and vowel-length breakdown for the target Afaan Oromoo phrase."),
                "strengths" to Schema.array(Schema.string("What the learner pronounced well.")),
                "improvements" to Schema.array(Schema.string("Specific sounds, vowel lengths, or ejectives (q, x, c, dh, ph) to refine."))
            )
        )
    }

    private val analysisModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = ANALYSIS_MODEL,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
                temperature = 0.25f
            }
        )
    }

    private val ttsModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = TTS_MODEL,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.AUDIO)
                speechConfig = SpeechConfig(voice = Voice("Kore"))
            }
        )
    }

    suspend fun generateModelPronunciationAudio(
        targetOromoText: String,
        phoneticGuide: String
    ): Result<ByteArray> = try {
        val pcmBytes = withTimeout(AI_TIMEOUT_MS) {
            val promptText = buildString {
                append("Read the following Afaan Oromoo word or phrase clearly at a natural educational pace. ")
                append("Observe double vowels (aa, ee, ii, oo, uu) and ejective/implosive consonants (q, x, c, dh, ph, ny). ")
                append("Text: \"$targetOromoText\". ")
                if (phoneticGuide.isNotBlank()) {
                    append("Phonetic guide: $phoneticGuide.")
                }
            }
            val response = ttsModel.generateContent(promptText)
            val audioPart = response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.filterIsInstance<InlineDataPart>()
                ?.firstOrNull()
                ?: error("AI audio model did not return audio data.")
            audioPart.inlineData
        }
        Result.success(pcmBytes)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "AI TTS generation failed: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun comparePronunciation(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4",
        targetOromo: String,
        englishMeaning: String,
        phoneticTip: String
    ): Result<PronunciationComparisonResult> = try {
        require(audioBytes.isNotEmpty()) { "Recorded audio is empty." }
        val result = withTimeout(AI_TIMEOUT_MS) {
            val prompt = content {
                inlineData(audioBytes, mimeType)
                text(
                    """
                    You are an expert Afaan Oromoo phonetic coach.
                    Listen carefully to the learner's audio recording and compare it with the target Afaan Oromoo expression:
                    Target Afaan Oromoo: "$targetOromo"
                    English meaning: "$englishMeaning"
                    Reference phonetic guide: "$phoneticTip"

                    Evaluate:
                    1. Clarity and intelligibility of the spoken words.
                    2. Vowel length contrasts (short vs. doubled vowels like a/aa, o/oo, u/uu).
                    3. Consonant articulation, especially Oromo sounds q, x, c, dh, ph, ny, sh, and gemination.
                    4. If the recording is silent, mostly noise, or completely unrelated words, score low (0-30) and kindly instruct them to speak clearly near the microphone.

                    Return accurate JSON matching the schema.
                    """.trimIndent()
                )
            }
            val response = analysisModel.generateContent(prompt)
            val rawJson = response.text?.trim()
                ?: error("Empty pronunciation feedback received from AI model.")
            parseFeedbackJson(rawJson, phoneticTip)
        }
        Result.success(result)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "AI pronunciation comparison failed: ${e.message}", e)
        Result.failure(e)
    }

    private fun parseFeedbackJson(rawJson: String, fallbackTip: String): PronunciationComparisonResult {
        val cleaned = rawJson
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val obj = JSONObject(cleaned)
        val strengthsArr = obj.optJSONArray("strengths")
        val improvementsArr = obj.optJSONArray("improvements")
        val strengths = buildList {
            if (strengthsArr != null) {
                for (i in 0 until strengthsArr.length()) {
                    strengthsArr.optString(i).takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }
        }
        val improvements = buildList {
            if (improvementsArr != null) {
                for (i in 0 until improvementsArr.length()) {
                    improvementsArr.optString(i).takeIf { it.isNotBlank() }?.let { add(it) }
                }
            }
        }
        val score = obj.optInt("accuracyScore", 0).coerceIn(0, 100)
        return PronunciationComparisonResult(
            accuracyScore = score,
            scoreLabel = obj.optString("scoreLabel", if (score >= 75) "Good" else "Keep Practicing"),
            feedbackOm = obj.optString("feedbackOm", "Yaalii gaarii! Sagalee dheeraa fi qubeewwan jabaatoo irra deebi'ii shaakali."),
            feedbackEn = obj.optString("feedbackEn", "Good effort! Replay the model pronunciation and focus on vowel length."),
            phoneticTip = obj.optString("phoneticTip", fallbackTip),
            strengths = strengths,
            improvements = improvements
        )
    }
}
