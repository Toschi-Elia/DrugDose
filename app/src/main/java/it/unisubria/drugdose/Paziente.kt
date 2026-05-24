package it.unisubria.drugdose
import com.google.firebase.firestore.DocumentId

data class Paziente(
    var id: String="",
    val nome: String = "",
    val cognome: String = "",
    val dataNascita: String = "",
    val peso: Double = 0.0,
    val altezza: Int = 0
)