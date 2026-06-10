package it.unisubria.drugdose.models
import com.google.firebase.firestore.DocumentId

data class Paziente(
    @DocumentId
    var id: String="",
    var nome: String = "",
    var cognome: String = "",
    var dataNascita: String = "",
    var peso: Double = 0.0,
    var altezza: Int = 0
)
