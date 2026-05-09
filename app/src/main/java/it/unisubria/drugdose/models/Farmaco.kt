package it.unisubria.drugdose.models

// Il modello base del farmaco.
data class Farmaco(
    val id: String = "",
    val nome_farmaco: String = "",
    val principio_attivo: String = "",
    val indicazione_clinica: String = "",
    val tipo_di_formula: String = "",
    val unita_di_misura: String = "",
    val durata_massima: String = "",
    val eta_minima: Int = 0,
    val alert: List<String> = emptyList(),
    val fonti: List<String> = emptyList(),
    
    // Opzionali (dipendono se è a dose fissa, a fasce, ecc.)
    val dosaggio_standard: DosaggioStandard? = null,
    val formati: List<Formato>? = null,
    val regole_calcolo: List<RegolaCalcolo>? = null
)

data class DosaggioStandard(
    val descrizione: String = "",
    val frequenza: String = ""
)

data class Formato(
    val tipo: String = "",
    val descrizione: String? = null,
    val limite_gravidanza: String? = null,
    val regole_calcolo: List<RegolaCalcolo>? = null
)

data class RegolaCalcolo(
    val fascia: String = "",
    val eta_min: Int = 0,
    val eta_max: Int = 999,
    val dose: String? = null, 
    val dose_singola_mg: Int? = null,
    val descrizione_dose: String? = null,
    val max_dosi_24h: Int? = null,
    val ripetibile_dopo_ore: String? = null
)