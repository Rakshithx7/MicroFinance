package com.example.microfinance.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat    = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun formatDate(millis: Long): String =
        if (millis <= 0) "—" else dateFormat.format(Date(millis))

    fun formatDateTime(millis: Long): String =
        if (millis <= 0) "—" else dateTimeFormat.format(Date(millis))

    fun formatDateOrNull(millis: Long?): String =
        if (millis == null || millis <= 0) "—" else dateFormat.format(Date(millis))
}
