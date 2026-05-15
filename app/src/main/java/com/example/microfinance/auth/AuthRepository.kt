package com.example.microfinance.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Handles Google Sign-In via Credential Manager + Firebase Authentication.
 *
 * SETUP REQUIRED:
 * Replace WEB_CLIENT_ID with your actual OAuth 2.0 Web Client ID from:
 * Firebase Console → Project Settings → General → Your apps → Web API Key
 * OR
 * Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs → Web client
 */
class AuthRepository(private val context: Context) {

    companion object {
        // ⚠️ REPLACE THIS with your actual Web Client ID from Firebase Console
        // Firebase Console → Project Settings → General → Web API Key
        // OR Google Cloud Console → OAuth 2.0 Client IDs → Web client (auto created by Google Service)
        const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com"
    }

    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val isSignedIn: Boolean get() = auth.currentUser != null

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    /**
     * Sign in with Google using Credential Manager.
     * Returns Result.success(FirebaseUser) on success, Result.failure on error.
     */
    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val user = authResult.user ?: return Result.failure(Exception("Sign-in failed: no user"))
            Result.success(user)

        } catch (e: GetCredentialException) {
            Result.failure(Exception("Google Sign-In cancelled or failed: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out from Firebase and clear Google credential state.
     */
    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) { /* ignore */ }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserName(): String? = auth.currentUser?.displayName

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun getCurrentUserPhotoUrl(): String? = auth.currentUser?.photoUrl?.toString()
}
