package com.example.microfinance.data.entity

enum class MemberStatus {
    GOOD_CONTRIBUTOR,   // ≥ 90 % consistency
    ACTIVE,             // 70–89 %
    IRREGULAR,          // 40–69 %
    PENDING             // < 40 % or has pending dues
}
