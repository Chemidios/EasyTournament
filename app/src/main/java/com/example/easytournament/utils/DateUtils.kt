package com.example.easytournament.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/* Objeto reutilizable para formatear fechas */
object DateUtils {

    /* Formateador siguiendo el estándar europeo (Día-Mes-Año) */
    @RequiresApi(Build.VERSION_CODES.O)
    private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale("es", "ES"))

    /* Transformador de cadenas de texto en textolegible */
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