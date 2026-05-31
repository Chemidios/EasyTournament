package com.example.easytournament.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/* Objeto reutilizable para formatear fechas */
object DateUtils {

    /* Formateador siguiendo el estándar europeo (Día-Mes-Año) */
    @RequiresApi(Build.VERSION_CODES.O)
    private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale("es", "ES"))

    /* Funciones para formatear fechas en España */
    @RequiresApi(Build.VERSION_CODES.O)
    val SPAIN_ZONE: ZoneId = ZoneId.of("Europe/Madrid")

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNowInSpain(): LocalDate {
        /* Devuelve la fecha actual en España, sin importar la zona horaria del dispositivo */
        return ZonedDateTime.now(SPAIN_ZONE).toLocalDate()
    }

    /* Transformador de cadenas de texto en textolegible */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatStandardDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Fecha no disponible"
        return try {
            val parsedDate = ZonedDateTime.parse(isoString).withZoneSameInstant(SPAIN_ZONE)
            parsedDate.format(displayFormatter)
        } catch (e: Exception) {
            isoString.split("T").firstOrNull() ?: isoString
        }
    }
}