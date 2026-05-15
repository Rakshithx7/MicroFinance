package com.example.microfinance.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.microfinance.data.db.DatabaseProvider
import com.example.microfinance.sync.FirestoreSyncRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    private val db       = DatabaseProvider.getDatabase(application)

    val currentUser: StateFlow<FirebaseUser?> = authRepo.currentUser

    private val _authState = MutableStateFlow<AuthState>(
        if (authRepo.isSignedIn) AuthState.SignedIn else AuthState.SignedOut
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // ── Sign In ───────────────────────────────────────────────────────────

    fun signInWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepo.signInWithGoogle()
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.SignedIn
                    // After sign-in, restore cloud data to local DB
                    restoreFromCloud(user.uid)
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Sign-in failed")
                }
            )
        }
    }

    // ── Sign Out ──────────────────────────────────────────────────────────

    fun signOut() {
        viewModelScope.launch {
            authRepo.signOut()
            _authState.value = AuthState.SignedOut
        }
    }

    // ── Cloud Restore ─────────────────────────────────────────────────────

    /**
     * After login: fetch all data from Firestore and insert into local Room DB.
     * Uses INSERT OR REPLACE (via @Insert with REPLACE strategy) so existing
     * local data is overwritten with cloud data.
     */
    private suspend fun restoreFromCloud(userId: String) {
        _syncState.value = SyncState.Syncing("Restoring your data from cloud...")
        try {
            val syncRepo = FirestoreSyncRepository(userId)
            val cloudData = syncRepo.fetchAllData()

            // Restore members
            cloudData.members.forEach { map ->
                val entity = with(syncRepo) { map.toMemberEntity() }
                if (entity.id > 0) db.memberDao().insert(entity)
            }

            // Restore savings
            cloudData.savings.forEach { map ->
                val entity = with(syncRepo) { map.toSavingsEntryEntity() }
                if (entity.id > 0) db.savingsDao().insert(entity)
            }

            // Restore loans
            cloudData.loans.forEach { map ->
                val entity = with(syncRepo) { map.toLoanEntity() }
                if (entity.id > 0) db.loanDao().insert(entity)
            }

            // Restore repayments
            cloudData.repayments.forEach { map ->
                val entity = with(syncRepo) { map.toRepaymentEntity() }
                if (entity.id > 0) db.repaymentDao().insert(entity)
            }

            // Restore group settings
            cloudData.settings?.let { map ->
                val entity = with(syncRepo) { map.toGroupSettingsEntity() }
                db.groupSettingsDao().upsert(entity)
            }

            _syncState.value = SyncState.Success("Data restored successfully!")
        } catch (e: Exception) {
            _syncState.value = SyncState.Error("Restore failed: ${e.message}")
        }
    }

    // ── Manual full backup ────────────────────────────────────────────────

    fun backupToCloud() {
        val userId = authRepo.getCurrentUserId() ?: return
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing("Backing up to cloud...")
            try {
                val syncRepo = FirestoreSyncRepository(userId)

                // Backup all members
                db.memberDao().getAllOnce().forEach { syncRepo.upsertMember(it) }

                // Backup all savings
                db.savingsDao().getAllOnce().forEach { syncRepo.upsertSavings(it) }

                // Backup all loans
                db.loanDao().getAllOnce().forEach { syncRepo.upsertLoan(it) }

                // Backup all repayments
                db.repaymentDao().getAllOnce().forEach { syncRepo.upsertRepayment(it) }

                // Backup group settings
                db.groupSettingsDao().getSettingsOnce()?.let { syncRepo.upsertGroupSettings(it) }

                _syncState.value = SyncState.Success("Backup complete!")
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Backup failed: ${e.message}")
            }
        }
    }

    fun clearSyncState() {
        _syncState.value = SyncState.Idle
    }

    // ── User info ─────────────────────────────────────────────────────────

    fun getUserName()     = authRepo.getCurrentUserName()
    fun getUserEmail()    = authRepo.getCurrentUserEmail()
    fun getUserPhotoUrl() = authRepo.getCurrentUserPhotoUrl()
    fun getUserId()       = authRepo.getCurrentUserId()
}

// ── State classes ─────────────────────────────────────────────────────────────

sealed class AuthState {
    object SignedOut : AuthState()
    object Loading  : AuthState()
    object SignedIn : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class SyncState {
    object Idle : SyncState()
    data class Syncing(val message: String) : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String)   : SyncState()
}
