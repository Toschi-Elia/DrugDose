package it.unisubria.drugdose.models

import java.util.Locale

// Il modello base del farmaco.
data class Farmaco(
    val id: String = "",
    val nome_farmaco: String = "",
    val principio_attivo: String = "",
    val principio_attivo_en: String = "",
    val indicazione_clinica: String = "",
    val indicazione_clinica_en: String = "",
    val tipo_di_formula: String = "",
    val unita_di_misura: String = "",
    val unita_di_misura_en: String = "",
    val durata_massima: String = "",
    val durata_massima_en: String = "",
    val eta_minima: Int = 0,
    val alert: List<String> = emptyList(),
    val alert_en: List<String> = emptyList(),
    val fonti: List<String> = emptyList(),
    
    // Opzionali (dipendono se è a dose fissa, a fasce, ecc.)
    val dosaggio_standard: DosaggioStandard? = null,
    val formati: List<Formato>? = null,
    val regole_calcolo: List<RegolaCalcolo>? = null
) {
    fun principioAttivoLocalizzato(languageCode: String): String {
        return testoLocalizzatoObbligatorio(principio_attivo, principio_attivo_en, languageCode)
    }

    fun indicazioneClinicaLocalizzata(languageCode: String): String {
        return testoLocalizzatoObbligatorio(indicazione_clinica, indicazione_clinica_en, languageCode)
    }

    fun unitaMisuraLocalizzata(languageCode: String): String {
        return testoLocalizzatoObbligatorio(unita_di_misura, unita_di_misura_en, languageCode)
    }

    fun tipoFormulaLocalizzato(languageCode: String): String {
        return etichettaTecnicaLocalizzata(tipo_di_formula, tipoFormulaLabels, languageCode)
    }

    fun durataMassimaLocalizzata(languageCode: String): String {
        return testoLocalizzatoObbligatorio(durata_massima, durata_massima_en, languageCode)
    }

    fun alertLocalizzati(languageCode: String): List<String> {
        return listaLocalizzata(alert, alert_en, languageCode)
    }
}

data class DosaggioStandard(
    val descrizione: String = "",
    val descrizione_en: String = "",
    val frequenza: String = "",
    val frequenza_en: String = ""
) {
    fun descrizioneLocalizzata(languageCode: String): String {
        return testoLocalizzatoObbligatorio(descrizione, descrizione_en, languageCode)
    }

    fun frequenzaLocalizzata(languageCode: String): String {
        return testoLocalizzatoObbligatorio(frequenza, frequenza_en, languageCode)
    }
}

data class Formato(
    val tipo: String = "",
    val descrizione: String? = null,
    val descrizione_en: String? = null,
    val limite_gravidanza: String? = null,
    val limite_gravidanza_en: String? = null,
    val regole_calcolo: List<RegolaCalcolo>? = null
) {
    fun descrizioneLocalizzata(languageCode: String): String? {
        return testoLocalizzato(descrizione, descrizione_en, languageCode)
    }

    fun tipoLocalizzato(languageCode: String): String {
        return etichettaTecnicaLocalizzata(tipo, formatoLabels, languageCode)
    }

    fun limiteGravidanzaLocalizzato(languageCode: String): String? {
        return testoLocalizzato(limite_gravidanza, limite_gravidanza_en, languageCode)
    }
}

data class RegolaCalcolo(
    val fascia: String = "",
    val fascia_en: String = "",
    val eta_min: Int = 0,
    val eta_max: Int = 999,
    val peso_min_kg: Double? = null,
    val peso_max_kg: Double? = null,
    val dose_per_kg: Double? = null,
    val dose_per_kg_min: Double? = null,
    val dose_per_kg_max: Double? = null,
    val dose_per_m2: Double? = null,
    val dose_per_m2_min: Double? = null,
    val dose_per_m2_max: Double? = null,
    val dose_fissa: Double? = null,
    val dose_carico_fissa: Double? = null,
    val dose: String? = null,
    val dose_en: String? = null,
    val dose_singola_mg: Int? = null,
    val descrizione_dose: String? = null,
    val descrizione_dose_en: String? = null,
    val max_dosi_24h: Int? = null,
    val ripetibile_dopo_ore: String? = null,
    val ripetibile_dopo_ore_en: String? = null
) {
    fun fasciaLocalizzata(languageCode: String): String {
        return testoLocalizzato(
            fascia,
            fascia_en.takeIf { it.isNotBlank() } ?: fasciaLabels[fascia],
            languageCode
        )?.replace('_', ' ').orEmpty()
    }

    fun doseLocalizzata(languageCode: String): String? {
        return testoLocalizzato(dose, dose_en, languageCode)
    }

    fun descrizioneDoseLocalizzata(languageCode: String): String? {
        return testoLocalizzato(descrizione_dose, descrizione_dose_en, languageCode)
    }

    fun ripetibileDopoOreLocalizzato(languageCode: String): String? {
        return testoLocalizzato(ripetibile_dopo_ore, ripetibile_dopo_ore_en, languageCode)
    }
}

private fun testoLocalizzato(
    testoItaliano: String?,
    testoInglese: String?,
    languageCode: String
): String? {
    return if (languageCode.lowercase(Locale.ROOT).startsWith("en") && !testoInglese.isNullOrBlank()) {
        testoInglese
    } else {
        testoItaliano
    }
}

private fun testoLocalizzatoObbligatorio(
    testoItaliano: String,
    testoInglese: String,
    languageCode: String
): String {
    return testoLocalizzato(testoItaliano, testoInglese, languageCode).orEmpty()
}

private fun listaLocalizzata(
    listaItaliana: List<String>,
    listaInglese: List<String>,
    languageCode: String
): List<String> {
    return if (languageCode.lowercase(Locale.ROOT).startsWith("en") && listaInglese.isNotEmpty()) {
        listaInglese
    } else {
        listaItaliana
    }
}

private fun etichettaTecnicaLocalizzata(
    value: String,
    englishLabels: Map<String, String>,
    languageCode: String
): String {
    return if (languageCode.lowercase(Locale.ROOT).startsWith("en")) {
        englishLabels[value] ?: value.replace('_', ' ')
    } else {
        value.replace('_', ' ')
    }
}

private val tipoFormulaLabels = mapOf(
    "bsa" to "BSA",
    "fasce_eta" to "age bands",
    "fasce_peso" to "weight bands",
    "fissa" to "fixed",
    "fissa_topica" to "fixed topical",
    "peso" to "weight-based"
)

private val formatoLabels = mapOf(
    "capsule_5mg" to "5 mg capsules",
    "fiale_15mg_2ml" to "15 mg/2 mL ampoules",
    "soluzione_infusione" to "infusion solution"
)

private val fasciaLabels = mapOf(
    "adulti" to "adults",
    "adulti_e_12plus" to "adults and 12+",
    "bambini_20_40kg" to "children 20-40 kg",
    "bambini_2_6_anni" to "children 2-6 years",
    "bambini_7_12_anni" to "children 7-12 years",
    "bambini_oltre_40kg" to "children over 40 kg",
    "dose_iniziale" to "initial dose",
    "iniziale" to "initial",
    "massima" to "maximum",
    "protocollo_frazionato" to "fractionated protocol",
    "protocollo_standard" to "standard protocol",
    "range_mantenimento" to "maintenance range"
)
