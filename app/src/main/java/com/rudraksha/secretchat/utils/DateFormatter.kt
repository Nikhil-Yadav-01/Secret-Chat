package com.rudraksha.secretchat.utils

import android.text.format.DateFormat
import java.util.Date

fun getCurrentTime(): String {
    val currentTimeMillis = System.currentTimeMillis()
    val date = DateFormat.format("hh:mm a", currentTimeMillis).toString()
    return date
}

fun getCurrentTimeWithSeconds(): String {
    val currentTimeMillis = System.currentTimeMillis()
    val date = DateFormat.format("hh:mm:ss a", currentTimeMillis).toString()
    return date
}