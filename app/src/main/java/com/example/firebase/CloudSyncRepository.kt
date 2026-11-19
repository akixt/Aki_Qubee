package com.example.firebase

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@Keep
@IgnoreExtraProperties
data class CloudUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val totalPoints: Long = 0L,
    val dailyGoalPoints: Long = 50L,
    val todayPoints: Long = 0L,
    val currentStreak: Long = 0L,
    val bestStreak: Long = 0L,
    val totalLearningSeconds: Long = 0L,
    val quizTotalAnswered: Long = 0L,
    val quizTotalCorrect: Long = 0L,
    val lastActiveDate: String = "",
    val unlockedAchievementIds: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudFavoriteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val itemType: String = "",
    val itemId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudQuizAttempt(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mode: String = "",
    val score: Long = 0L,
    val totalQuestions: Long = 1L,
    val pointsEarned: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CloudRestoreBundle(
    val profile: CloudUserProfile?,
    val favoriteProverbIds: Set<String>,
    val favoritePhraseIds: Set<String>,
    val favoriteVocabIds: Set<String>
)

class CloudSyncRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val TAG = "CloudSyncRepo"
    }

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private fun resolveWebClientId(context: Context): String? {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) context.getString(resId).takeIf { it.isNotBlank() } else null
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> = try {
        val serverClientId = resolveWebClientId(activityContext)
            ?: error("Google Sign-In client configuration is not available in this build.")
        val credentialManager = CredentialManager.create(activityContext)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        val result = try {
            credentialManager.getCredential(activityContext, request)
        } catch (primaryEx: Exception) {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .build()
            val fallbackRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            credentialManager.getCredential(activityContext, fallbackRequest)
        }

        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCred = GoogleAuthProvider.getCredential(googleToken.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCred).await()
            val user = authResult.user ?: error("Signed-in user was null.")
            Result.success(user)
        } else {
            error("Unsupported credential returned by Credential Manager.")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Google Sign-In failed: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun signOut(context: Context) {
        auth.signOut()
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    suspend fun syncUserProfile(
        displayName: String,
        totalPoints: Int,
        dailyGoalPoints: Int,
        todayPoints: Int,
        currentStreak: Int,
        bestStreak: Int,
        totalLearningSeconds: Long,
        quizTotalAnswered: Int,
        quizTotalCorrect: Int,
        lastActiveDate: String,
        unlockedAchievementIds: List<String>
    ): Result<Unit> = try {
        val user = auth.currentUser ?: error("Sign in with Google before syncing to cloud.")
        val docRef = db.collection("users").document(user.uid)
        val existing = docRef.get().await()
        if (!existing.exists()) {
            val payload = mapOf(
                "userId" to user.uid,
                "displayName" to displayName.ifBlank { user.displayName ?: "Aki Learner" },
                "totalPoints" to totalPoints.toLong(),
                "dailyGoalPoints" to dailyGoalPoints.toLong(),
                "todayPoints" to todayPoints.toLong(),
                "currentStreak" to currentStreak.toLong(),
                "bestStreak" to bestStreak.toLong(),
                "totalLearningSeconds" to totalLearningSeconds,
                "quizTotalAnswered" to quizTotalAnswered.toLong(),
                "quizTotalCorrect" to quizTotalCorrect.toLong(),
                "lastActiveDate" to lastActiveDate,
                "unlockedAchievementIds" to unlockedAchievementIds,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } else {
            val updates = mapOf(
                "displayName" to displayName.ifBlank { user.displayName ?: "Aki Learner" },
                "totalPoints" to totalPoints.toLong(),
                "dailyGoalPoints" to dailyGoalPoints.toLong(),
                "todayPoints" to todayPoints.toLong(),
                "currentStreak" to currentStreak.toLong(),
                "bestStreak" to bestStreak.toLong(),
                "totalLearningSeconds" to totalLearningSeconds,
                "quizTotalAnswered" to quizTotalAnswered.toLong(),
                "quizTotalCorrect" to quizTotalCorrect.toLong(),
                "lastActiveDate" to lastActiveDate,
                "unlockedAchievementIds" to unlockedAchievementIds,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updates).await()
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        handleFirestoreError(e, OperationType.WRITE, "users/$uid")
        Result.failure(e)
    }

    suspend fun syncFavorite(itemType: String, itemId: String, isFavorite: Boolean): Result<Unit> = try {
        val user = auth.currentUser ?: error("Sign in with Google before syncing favorites.")
        val safeDocId = "${itemType}_${itemId}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        val docRef = db.collection("users").document(user.uid)
            .collection("favorites").document(safeDocId)
        if (isFavorite) {
            val existing = docRef.get().await()
            if (!existing.exists()) {
                docRef.set(
                    mapOf(
                        "userId" to user.uid,
                        "itemType" to itemType,
                        "itemId" to itemId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } else {
            docRef.delete().await()
        }
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        handleFirestoreError(e, OperationType.WRITE, "users/$uid/favorites")
        Result.failure(e)
    }

    suspend fun uploadQuizAttempt(mode: String, score: Int, totalQuestions: Int, pointsEarned: Int): Result<Unit> = try {
        val user = auth.currentUser ?: error("Sign in with Google before syncing quiz history.")
        val collectionRef = db.collection("users").document(user.uid).collection("quiz_attempts")
        collectionRef.add(
            mapOf(
                "userId" to user.uid,
                "mode" to mode,
                "score" to score.toLong(),
                "totalQuestions" to totalQuestions.coerceAtLeast(1).toLong(),
                "pointsEarned" to pointsEarned.toLong(),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        handleFirestoreError(e, OperationType.CREATE, "users/$uid/quiz_attempts")
        Result.failure(e)
    }

    suspend fun pullCloudBackup(): Result<CloudRestoreBundle> = try {
        val user = auth.currentUser ?: error("Sign in with Google to restore backup.")
        val userDoc = db.collection("users").document(user.uid).get().await()
        val profile = userDoc.toObject(CloudUserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        val favSnap = db.collection("users").document(user.uid).collection("favorites").get().await()
        val favItems = favSnap.toObjects(CloudFavoriteItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        Result.success(
            CloudRestoreBundle(
                profile = profile,
                favoriteProverbIds = favItems.filter { it.itemType == "PROVERB" }.map { it.itemId }.toSet(),
                favoritePhraseIds = favItems.filter { it.itemType == "PHRASE" }.map { it.itemId }.toSet(),
                favoriteVocabIds = favItems.filter { it.itemType == "VOCAB" }.map { it.itemId }.toSet()
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val uid = auth.currentUser?.uid ?: "unauthenticated"
        handleFirestoreError(e, OperationType.GET, "users/$uid")
        Result.failure(e)
    }
}
