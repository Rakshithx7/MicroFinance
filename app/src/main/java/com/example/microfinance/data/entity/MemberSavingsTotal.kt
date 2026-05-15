package com.example.microfinance.data.entity

data class MemberSavingsTotal(
    val memberId: Long,
    val memberName: String,
    val totalPaid: Double,
    val totalPending: Double,
    val paidCount: Int,
    val pendingCount: Int
)
