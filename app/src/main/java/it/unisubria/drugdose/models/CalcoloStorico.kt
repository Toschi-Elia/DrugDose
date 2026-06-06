package it.unisubria.drugdose.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class CalcoloStorico(
    @DocumentId
    var id: String = "",
    var dataOra: Timestamp? = null,

    var pazienteId: String = "",
    var pazienteNome: String = "",
    var pazienteDataNascita: String = "",
    var pazientePesoKg: Double = 0.0,
    var pazienteAltezzaCm: Int = 0,

    var farmacoId: String = "",
    var farmacoNome: String = "",
    var principioAttivo: String = "",

    var schema: String = "",
    var doseValore: String = "",
    var doseUnita: String = "",
    var frequenza: String = ""
)
