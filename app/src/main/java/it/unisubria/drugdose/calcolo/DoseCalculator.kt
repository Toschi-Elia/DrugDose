package it.unisubria.drugdose.calcolo

import it.unisubria.drugdose.models.DosaggioStandard
import it.unisubria.drugdose.models.Farmaco
import it.unisubria.drugdose.models.Formato
import it.unisubria.drugdose.models.RegolaCalcolo
import java.util.Locale

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
        pesoKg: Double
    ): DoseCalcolata? {
        return when {
            regola?.dose_per_kg != null -> {
                DoseCalcolata(
                    valore = formattaNumero(calcolaDosePerKg(regola.dose_per_kg, pesoKg)),
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard)
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
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard)
                )
            }

            regola?.dose_carico_fissa != null && regola.dose_fissa != null -> {
                DoseCalcolata(
                    valore = "Dose iniziale: ${formattaNumero(regola.dose_carico_fissa)} mg\n" +
                            "Mantenimento: ${formattaNumero(regola.dose_fissa)} mg",
                    unita = null,
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard),
                    descrittivo = true
                )
            }

            regola?.dose_fissa != null -> {
                DoseCalcolata(
                    valore = formattaNumero(calcolaDoseFissa(regola.dose_fissa)),
                    unita = "mg",
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard)
                )
            }

            formato?.descrizione != null -> {
                DoseCalcolata(
                    valore = formato.descrizione,
                    unita = null,
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard),
                    descrittivo = true
                )
            }

            dosaggioStandard != null || farmaco.dosaggio_standard != null -> {
                val dosaggio = dosaggioStandard ?: farmaco.dosaggio_standard
                DoseCalcolata(
                    valore = dosaggio?.descrizione.orEmpty(),
                    unita = null,
                    frequenza = frequenzaRisultato(regola, farmaco, dosaggioStandard),
                    descrittivo = true
                )
            }

            else -> null
        }
    }

    private fun frequenzaRisultato(
        regola: RegolaCalcolo?,
        farmaco: Farmaco,
        dosaggioStandard: DosaggioStandard?
    ): String {
        return regola?.descrizione_dose
            ?: regola?.dose
            ?: dosaggioStandard?.frequenza
            ?: farmaco.dosaggio_standard?.frequenza
            ?: "--"
    }

    private fun formattaNumero(numero: Double): String {
        return if (numero % 1.0 == 0.0) {
            numero.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", numero)
        }
    }
}
