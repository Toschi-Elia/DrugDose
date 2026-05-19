package it.unisubria.drugdose

data class Paziente(
    val nome: String = "",
    val cognome: String = "",
    val dataNascita: String = "",
    val peso: Double = 0.0,
    val altezza: Int = 0
)