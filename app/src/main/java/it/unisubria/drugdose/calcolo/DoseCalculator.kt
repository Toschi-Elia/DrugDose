package it.unisubria.drugdose.calcolo

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
}
