package it.unisubria.drugdose.util

import android.util.Patterns
import java.time.LocalDate
import java.time.temporal.ChronoUnit


 class Utils {


}
fun String.isValidEmail(): Boolean
{
    return this.trim().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this.trim()).matches()
}
fun String.isStrongPassword():Boolean{
    val passwordRegex= "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_\\-])(?=\\S+$).{8,}$"
    return this.matches(passwordRegex.toRegex())
}

/**
 * pulisce la stringa e mette solo il primo carattere maiuscolo
 */
fun String.formattaMaiusc(): String {

    return this.trim().lowercase().replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString()
    }
}

fun calcolaEtaDaDataNascita(dataNascita: String): Int? {
    return try {
        val nascita = LocalDate.parse(dataNascita)
        ChronoUnit.YEARS.between(nascita, LocalDate.now()).toInt()
    } catch (e: Exception) {
        null
    }
}
