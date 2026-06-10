package it.unisubria.drugdose.calcolo

import it.unisubria.drugdose.models.DosaggioStandard
import it.unisubria.drugdose.models.Farmaco
import it.unisubria.drugdose.models.Formato
import it.unisubria.drugdose.models.RegolaCalcolo
import java.util.Locale
import kotlin.math.sqrt

data class DoseCalcolata(
    val valore: String,
    val unita: String?,
    val frequenza: String,
    val descrittivo: Boolean = false
)

object DoseCalculator {

    fun calcolaDosePerKg(dosePerKg: Double, pesoKg: Double): Double {
        return dosePerKg * pesoKg
    }

    fun calcolaRangeDosePerKg(
        dosePerKgMin: Double,
        dosePerKgMax: Double,
        pesoKg: Double
    ): Pair<Double, Double> {
        val doseMin = dosePerKgMin * pesoKg
        val doseMax = dosePerKgMax * pesoKg
        return Pair(doseMin, doseMax)
    }

    fun calcolaBsa(pesoKg: Double, altezzaCm: Int): Double {
        return sqrt((altezzaCm * pesoKg) / 3600.0)
    }

    fun calcolaDosePerM2(dosePerM2: Double, bsa: Double): Double {
        return dosePerM2 * bsa
    }

    fun calcolaRangeDosePerM2(
        dosePerM2Min: Double,
        dosePerM2Max: Double,
        bsa: Double
    ): Pair<Double, Double> {
        val doseMin = dosePerM2Min * bsa
        val doseMax = dosePerM2Max * bsa
        return Pair(doseMin, doseMax)
    }

    fun etaCompatibile(etaAnni: Int, etaMin: Int, etaMax: Int): Boolean {
        return etaAnni in etaMin..etaMax
    }

    fun pesoCompatibile(
        pesoKg: Double,
        pesoMinKg: Double?,
        pesoMaxKg: Double?
    ): Boolean {
        val sopraMinimo = pesoMinKg == null || pesoKg >= pesoMinKg
        val sottoMassimo = pesoMaxKg == null || pesoKg < pesoMaxKg
        return sopraMinimo && sottoMassimo
    }

    fun calcolaDoseFissa(doseFissa: Double): Double {
        return doseFissa
    }

    fun calcolaDoseDaRegola(
        farmaco: Farmaco,
        regola: RegolaCalcolo?,
        formato: Formato?,
        dosaggioStandard: DosaggioStandard?,
        pesoKg: Double,
        altezzaCm: Int,
        languageCode: String = Locale.getDefault().language
    ): DoseCalcolata? {
        return when {
            regola?.dose_per_kg != null -> {
                DoseCalcolata(
                    valore = formattaNumero(calcolaDosePerKg(regola.dose_per_kg, pesoKg)),
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode)
                )
            }

            regola?.dose_per_kg_min != null && regola.dose_per_kg_max != null -> {
                val range = calcolaRangeDosePerKg(
                    regola.dose_per_kg_min,
                    regola.dose_per_kg_max,
                    pesoKg
                )
                DoseCalcolata(
                    valore = "${formattaNumero(range.first)}-${formattaNumero(range.second)}",
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode)
                )
            }

            regola?.dose_per_m2 != null -> {
                val bsa = calcolaBsa(pesoKg, altezzaCm)
                DoseCalcolata(
                    valore = formattaNumero(calcolaDosePerM2(regola.dose_per_m2, bsa)),
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode)
                )
            }

            regola?.dose_per_m2_min != null && regola.dose_per_m2_max != null -> {
                val bsa = calcolaBsa(pesoKg, altezzaCm)
                val range = calcolaRangeDosePerM2(
                    regola.dose_per_m2_min,
                    regola.dose_per_m2_max,
                    bsa
                )
                DoseCalcolata(
                    valore = "${formattaNumero(range.first)}-${formattaNumero(range.second)}",
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode)
                )
            }

            regola?.dose_carico_fissa != null && regola.dose_fissa != null -> {
                DoseCalcolata(
                    valore = "${etichettaDoseIniziale(languageCode)}: ${formattaNumero(regola.dose_carico_fissa)} mg\n" +
                            "${etichettaMantenimento(languageCode)}: ${formattaNumero(regola.dose_fissa)} mg",
                    unita = null,
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode),
                    descrittivo = true
                )
            }

            regola?.dose_fissa != null -> {
                DoseCalcolata(
                    valore = formattaNumero(calcolaDoseFissa(regola.dose_fissa)),
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode)
                )
            }

            formato?.descrizioneLocalizzata(languageCode) != null -> {
                DoseCalcolata(
                    valore = formato.descrizioneLocalizzata(languageCode).orEmpty(),
                    unita = null,
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode),
                    descrittivo = true
                )
            }

            dosaggioStandard != null || farmaco.dosaggio_standard != null -> {
                val dosaggio = dosaggioStandard ?: farmaco.dosaggio_standard
                DoseCalcolata(
                    valore = dosaggio?.descrizioneLocalizzata(languageCode).orEmpty(),
                    unita = null,
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard, languageCode),
                    descrittivo = true
                )
            }

            else -> null
        }
    }

    private fun frequenzaRisultato(
        regola: RegolaCalcolo?,
        farmaco: Farmaco,
        dosaggioStandard: DosaggioStandard?,
        languageCode: String
    ): String {
        return regola?.descrizioneDoseLocalizzata(languageCode)
            ?: regola?.doseLocalizzata(languageCode)
            ?: dosaggioStandard?.frequenzaLocalizzata(languageCode)
            ?: farmaco.dosaggio_standard?.frequenzaLocalizzata(languageCode)
            ?: "--"
    }

    private fun etichettaDoseIniziale(languageCode: String): String {
        return if (languageCode.lowercase(Locale.ROOT).startsWith("en")) "Initial dose" else "Dose iniziale"
    }

    private fun etichettaMantenimento(languageCode: String): String {
        return if (languageCode.lowercase(Locale.ROOT).startsWith("en")) "Maintenance" else "Mantenimento"
    }

    private fun formattaNumero(numero: Double): String {
        return if (numero % 1.0 == 0.0) {
            numero.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", numero)
        }
    }
}
