package com.example.easytournament.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale("es", "ES"))

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatStandardDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Fecha no disponible"
        return try {
            val parsedDate = ZonedDateTime.parse(isoString)
            parsedDate.format(displayFormatter)
        } catch (e: Exception) {
            isoString.split("T").firstOrNull() ?: isoString
        }
    }
}