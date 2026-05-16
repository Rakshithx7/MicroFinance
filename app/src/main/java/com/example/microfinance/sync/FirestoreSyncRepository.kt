package com.example.microfinance.sync

import com.example.microfinance.data.entity.GroupSettingsEntity
import com.example.microfinance.data.entity.LoanEntity
import com.example.microfinance.data.entity.MemberEntity
import com.example.microfinance.data.entity.RepaymentEntity
import com.example.microfinance.data.entity.SavingsEntryEntity
import com.example.microfinance.data.entity.SavingsStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Firestore data structure per user:
 *
 * users/{userId}/
 *   members/{memberId}        → MemberEntity fields
 *   savings/{savingsId}       → SavingsEntryEntity fields
 *   loans/{loanId}            → LoanEntity fields
 *   repayments/{repaymentId}  → RepaymentEntity fields
 *   settings/group            → GroupSettingsEntity fields
 *
 * All IDs use the local Room auto-generated Long ID as the document ID string.
 * This ensures idempotent upserts — re-syncing the same record is safe.
 */
class FirestoreSyncRepository(private val userId: String) {

    private val db = FirebaseFirestore.getInstance()
    private val userDoc get() = db.collection("users").document(userId)

    // ── Member sync ───────────────────────────────────────────────────────

    suspend fun upsertMember(member: MemberEntity) {
        userDoc.collection("members").document(member.id.toString())
            .set(member.toMap(), SetOptions.merge()).await()
    }

    suspend fun deleteMember(memberId: Long) {
        userDoc.collection("members").document(memberId.toString()).delete().await()
    }

    suspend fun fetchAllMembers(): List<Map<String, Any>> {
        return userDoc.collection("members").get().await()
            .documents.mapNotNull { it.data }
    }

    // ── Savings sync ──────────────────────────────────────────────────────

    suspend fun upsertSavings(entry: SavingsEntryEntity) {
        userDoc.collection("savings").document(entry.id.toString())
            .set(entry.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchAllSavings(): List<Map<String, Any>> {
        return userDoc.collection("savings").get().await()
            .documents.mapNotNull { it.data }
    }

    // ── Loan sync ─────────────────────────────────────────────────────────

    suspend fun upsertLoan(loan: LoanEntity) {
        userDoc.collection("loans").document(loan.id.toString())
            .set(loan.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchAllLoans(): List<Map<String, Any>> {
        return userDoc.collection("loans").get().await()
            .documents.mapNotNull { it.data }
    }

    // ── Repayment sync ────────────────────────────────────────────────────

    suspend fun upsertRepayment(repayment: RepaymentEntity) {
        userDoc.collection("repayments").document(repayment.id.toString())
            .set(repayment.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchAllRepayments(): List<Map<String, Any>> {
        return userDoc.collection("repayments").get().await()
            .documents.mapNotNull { it.data }
    }

    // ── Group settings sync ───────────────────────────────────────────────

    suspend fun upsertGroupSettings(settings: GroupSettingsEntity) {
        userDoc.collection("settings").document("group")
            .set(settings.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchGroupSettings(): Map<String, Any>? {
        return userDoc.collection("settings").document("group").get().await().data
    }

    // ── Full restore ──────────────────────────────────────────────────────

    /**
     * Fetches all cloud data and returns it as [CloudData].
     * Called after login to restore data to local Room DB.
     */
    suspend fun fetchAllData(): CloudData {
        return CloudData(
            members    = fetchAllMembers(),
            savings    = fetchAllSavings(),
            loans      = fetchAllLoans(),
            repayments = fetchAllRepayments(),
            settings   = fetchGroupSettings()
        )
    }

    // ── Conversion helpers ────────────────────────────────────────────────

    private fun MemberEntity.toMap() = mapOf(
        "id"              to id,
        "name"            to name,
        "phone"           to phone,
        "photoUri"        to (photoUri ?: ""),
        "createdAtMillis" to createdAtMillis
    )

    private fun SavingsEntryEntity.toMap() = mapOf(
        "id"              to id,
        "memberId"        to memberId,
        "amount"          to amount,
        "status"          to status.name,
        "entryDateMillis" to entryDateMillis,
        "weekNumber"      to weekNumber,
        "weekYear"        to weekYear
    )

    private fun LoanEntity.toMap() = mapOf(
        "id"                  to id,
        "memberId"            to memberId,
        "principalAmount"     to principalAmount,
        "interestRatePercent" to interestRatePercent,
        "startDateMillis"     to startDateMillis,
        "durationMonths"      to durationMonths,
        "dueDateMillis"       to dueDateMillis,
        "isClosed"            to isClosed
    )

    private fun RepaymentEntity.toMap() = mapOf(
        "id"           to id,
        "loanId"       to loanId,
        "amount"       to amount,
        "paidAtMillis" to paidAtMillis
    )

    private fun GroupSettingsEntity.toMap() = mapOf(
        "id"                  to id,
        "groupName"           to groupName,
        "weeklyContribution"  to weeklyContribution,
        "groupStartMillis"    to groupStartMillis
    )

    // ── Cloud data container ──────────────────────────────────────────────

    data class CloudData(
        val members:    List<Map<String, Any>>,
        val savings:    List<Map<String, Any>>,
        val loans:      List<Map<String, Any>>,
        val repayments: List<Map<String, Any>>,
        val settings:   Map<String, Any>?
    )

    // ── Restore helpers ───────────────────────────────────────────────────

    fun Map<String, Any>.toMemberEntity() = MemberEntity(
        id              = (get("id") as? Long) ?: (get("id") as? Number)?.toLong() ?: 0L,
        name            = get("name") as? String ?: "",
        phone           = get("phone") as? String ?: "",
        photoUri        = (get("photoUri") as? String)?.takeIf { it.isNotBlank() },
        createdAtMillis = (get("createdAtMillis") as? Long) ?: (get("createdAtMillis") as? Number)?.toLong() ?: 0L
    )

    fun Map<String, Any>.toSavingsEntryEntity() = SavingsEntryEntity(
        id              = (get("id") as? Long) ?: (get("id") as? Number)?.toLong() ?: 0L,
        memberId        = (get("memberId") as? Long) ?: (get("memberId") as? Number)?.toLong() ?: 0L,
        amount          = (get("amount") as? Double) ?: (get("amount") as? Number)?.toDouble() ?: 0.0,
        status          = SavingsStatus.valueOf(get("status") as? String ?: "PAID"),
        entryDateMillis = (get("entryDateMillis") as? Long) ?: (get("entryDateMillis") as? Number)?.toLong() ?: 0L,
        weekNumber      = (get("weekNumber") as? Long)?.toInt() ?: (get("weekNumber") as? Number)?.toInt() ?: 0,
        weekYear        = (get("weekYear") as? Long)?.toInt() ?: (get("weekYear") as? Number)?.toInt() ?: 0
    )

    fun Map<String, Any>.toLoanEntity() = LoanEntity(
        id                  = (get("id") as? Long) ?: (get("id") as? Number)?.toLong() ?: 0L,
        memberId            = (get("memberId") as? Long) ?: (get("memberId") as? Number)?.toLong() ?: 0L,
        principalAmount     = (get("principalAmount") as? Double) ?: (get("principalAmount") as? Number)?.toDouble() ?: 0.0,
        interestRatePercent = (get("interestRatePercent") as? Double) ?: (get("interestRatePercent") as? Number)?.toDouble() ?: 0.0,
        startDateMillis     = (get("startDateMillis") as? Long) ?: (get("startDateMillis") as? Number)?.toLong() ?: 0L,
        durationMonths      = (get("durationMonths") as? Long)?.toInt() ?: (get("durationMonths") as? Number)?.toInt() ?: 0,
        dueDateMillis       = (get("dueDateMillis") as? Long) ?: (get("dueDateMillis") as? Number)?.toLong() ?: 0L,
        isClosed            = get("isClosed") as? Boolean ?: false
    )

    fun Map<String, Any>.toRepaymentEntity() = RepaymentEntity(
        id           = (get("id") as? Long) ?: (get("id") as? Number)?.toLong() ?: 0L,
        loanId       = (get("loanId") as? Long) ?: (get("loanId") as? Number)?.toLong() ?: 0L,
        amount       = (get("amount") as? Double) ?: (get("amount") as? Number)?.toDouble() ?: 0.0,
        paidAtMillis = (get("paidAtMillis") as? Long) ?: (get("paidAtMillis") as? Number)?.toLong() ?: 0L
    )

    fun Map<String, Any>.toGroupSettingsEntity() = GroupSettingsEntity(
        id                 = (get("id") as? Long)?.toInt() ?: 1,
        groupName          = get("groupName") as? String ?: "Mahila-Shakti Unnati",
        weeklyContribution = (get("weeklyContribution") as? Double) ?: (get("weeklyContribution") as? Number)?.toDouble() ?: 500.0,
        groupStartMillis   = (get("groupStartMillis") as? Long) ?: (get("groupStartMillis") as? Number)?.toLong() ?: System.currentTimeMillis()
    )
}
