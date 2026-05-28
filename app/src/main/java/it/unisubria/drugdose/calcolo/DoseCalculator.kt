package it.unisubria.drugdose.calcolo

object DoseCalculator {

    fun calcolaDosePerKg(dosePerKg: Double, pesoKg: Double): Double {
        return dosePerKg * pesoKg
    }
}
