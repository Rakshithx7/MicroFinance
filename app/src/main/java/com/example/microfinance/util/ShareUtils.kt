package com.example.microfinance.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, "Share via")
        context.startActivity(chooser)
    }
}
