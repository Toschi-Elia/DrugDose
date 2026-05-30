package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import it.unisubria.drugdose.calcolo.DoseCalculator
import it.unisubria.drugdose.databinding.FragmentHomeBinding
import it.unisubria.drugdose.models.DosaggioStandard
import it.unisubria.drugdose.models.Farmaco
import it.unisubria.drugdose.models.Formato
import it.unisubria.drugdose.models.RegolaCalcolo
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var farmaciViewModel: FarmaciViewModel
    private lateinit var pazientiViewModel: PazientiViewModel

    // Dati del farmaco selezionato
    private var farmacoSelezionato: Farmaco? = null
    private var formatoSelezionato: Formato? = null
    private var dosaggioStandardSelezionato: DosaggioStandard? = null
    private var regolaSelezionata: RegolaCalcolo? = null

    // Dati del paziente in arrivo dalla lista
    private var pazienteId: String? = null
    private var pazienteNome: String? = null
    private var pazienteDataNascita: String? = null
    private var pazientePeso: Double = 0.0
    private var pazienteAltezza: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            pazienteId = bundle.getString("ID_PAZIENTE")
            pazienteNome = bundle.getString("NOME_PAZIENTE")
            pazienteDataNascita = bundle.getString("DATA_NASCITA_PAZIENTE")
            pazientePeso = bundle.getDouble("PESO_PAZIENTE", 0.0)
            pazienteAltezza = bundle.getInt("ALTEZZA_PAZIENTE", 0)


            if (!pazienteNome.isNullOrEmpty()) {
                binding.dropdownPaziente.setText(pazienteNome, false)
            } else {
                binding.dropdownPaziente.setText("", false)
            }
        }

        pazientiViewModel = ViewModelProvider(requireActivity())[PazientiViewModel::class.java]
        farmaciViewModel = ViewModelProvider(requireActivity())[FarmaciViewModel::class.java]

        binding.dropdownPaziente.threshold = 0

        pazientiViewModel.listaPazienti.observe(viewLifecycleOwner) { listaPazienti ->
            val elementiPaziente = listaPazienti.map { PazienteDropdownItem(it) }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                elementiPaziente
            )
            binding.dropdownPaziente.setAdapter(adapter)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            farmaciViewModel.farmaci.collect { listaFarmaci ->
                val elementiFarmaco = listaFarmaci.map { FarmacoDropdownItem(it) }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    elementiFarmaco
                )
                binding.dropdownFarmaco.setAdapter(adapter)
            }
        }

        binding.dropdownPaziente.setOnClickListener {
            binding.dropdownPaziente.showDropDown()
        }

        binding.dropdownPaziente.setOnItemClickListener { _, _, position, _ ->
            val elemento = binding.dropdownPaziente.adapter.getItem(position) as? PazienteDropdownItem
            val paziente = elemento?.paziente

            pazienteId = paziente?.id
            pazienteNome = paziente?.let { "${it.nome} ${it.cognome}" }
            pazienteDataNascita = paziente?.dataNascita
            pazientePeso = paziente?.peso ?: 0.0
            pazienteAltezza = paziente?.altezza ?: 0

            aggiornaDropdownSchemi(binding.dropdownFormato, farmacoSelezionato)
        }

        binding.dropdownFarmaco.setOnItemClickListener { _, _, position, _ ->
            val elemento = binding.dropdownFarmaco.adapter.getItem(position) as? FarmacoDropdownItem
            farmacoSelezionato = elemento?.farmaco
            aggiornaDropdownSchemi(binding.dropdownFormato, farmacoSelezionato)
        }

        farmaciViewModel.caricaFarmaci()

        binding.btnCalcolaDose.setOnClickListener {
            if (pazientePeso <= 0.0) {
                Toast.makeText(requireContext(), "Seleziona un paziente valido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (farmacoSelezionato == null) {
                Toast.makeText(requireContext(), "Seleziona un farmaco", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mostraCalcoloDose()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun aggiornaDropdownSchemi(
        dropdownSchema: AutoCompleteTextView,
        farmaco: Farmaco?
    ) {
        formatoSelezionato = null
        dosaggioStandardSelezionato = null
        regolaSelezionata = null

        val elementiSchema = creaElementiSchema(farmaco)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            elementiSchema
        )
        dropdownSchema.setAdapter(adapter)
        dropdownSchema.setText("", false)

        if (elementiSchema.size == 1) {
            selezionaSchema(elementiSchema.first())
            dropdownSchema.setText(elementiSchema.first().toString(), false)
        }

        dropdownSchema.setOnItemClickListener { _, _, position, _ ->
            val elemento = dropdownSchema.adapter.getItem(position) as? SchemaDropdownItem
            if (elemento != null) {
                selezionaSchema(elemento)
            }
        }
    }

    private fun creaElementiSchema(farmaco: Farmaco?): List<SchemaDropdownItem> {
        if (farmaco == null) return emptyList()

        val tuttiGliElementi = mutableListOf<SchemaDropdownItem>()

        if (!farmaco.regole_calcolo.isNullOrEmpty()) {
            farmaco.regole_calcolo.forEach { regola ->
                tuttiGliElementi.add(SchemaDropdownItem(regola = regola))
            }
        } else if (!farmaco.formati.isNullOrEmpty()) {
            farmaco.formati.forEach { formato ->
                if (!formato.regole_calcolo.isNullOrEmpty()) {
                    formato.regole_calcolo.forEach { regola ->
                        tuttiGliElementi.add(
                            SchemaDropdownItem(
                                formato = formato,
                                regola = regola
                            )
                        )
                    }
                } else {
                    tuttiGliElementi.add(SchemaDropdownItem(formato = formato))
                }
            }
        } else if (farmaco.dosaggio_standard != null) {
            tuttiGliElementi.add(
                SchemaDropdownItem(
                    dosaggioStandard = farmaco.dosaggio_standard
                )
            )
        }

        val etaPaziente = pazienteDataNascita?.let { calcolaEtaDaDataNascita(it) }
        if (pazientePeso <= 0.0 && etaPaziente == null) return tuttiGliElementi

        val elementiCompatibili = tuttiGliElementi.filter { elemento ->
            val regola = elemento.regola ?: return@filter true
            val etaCompatibile = etaPaziente == null ||
                    DoseCalculator.etaCompatibile(etaPaziente, regola.eta_min, regola.eta_max)
            val pesoCompatibile = pazientePeso <= 0.0 ||
                    DoseCalculator.pesoCompatibile(pazientePeso, regola.peso_min_kg, regola.peso_max_kg)
            etaCompatibile && pesoCompatibile
        }

        return elementiCompatibili.ifEmpty { tuttiGliElementi }
    }

    private fun selezionaSchema(elemento: SchemaDropdownItem) {
        formatoSelezionato = elemento.formato
        dosaggioStandardSelezionato = elemento.dosaggioStandard
        regolaSelezionata = elemento.regola
    }

    private fun mostraCalcoloDose() {
        val farmaco = farmacoSelezionato ?: return
        val regola = regolaSelezionata

        val risultato = when {
            regola?.dose_per_kg != null -> {
                formattaNumero(
                    DoseCalculator.calcolaDosePerKg(
                        regola.dose_per_kg,
                        pazientePeso
                    )
                )
            }

            regola?.dose_per_kg_min != null && regola.dose_per_kg_max != null -> {
                val range = DoseCalculator.calcolaRangeDosePerKg(
                    regola.dose_per_kg_min,
                    regola.dose_per_kg_max,
                    pazientePeso
                )
                "${formattaNumero(range.first)}-${formattaNumero(range.second)}"
            }

            regola?.dose_fissa != null -> {
                formattaNumero(DoseCalculator.calcolaDoseFissa(regola.dose_fissa))
            }

            formatoSelezionato?.descrizione != null -> {
                formatoSelezionato?.descrizione.orEmpty()
            }

            dosaggioStandardSelezionato != null || farmaco.dosaggio_standard != null -> {
                (dosaggioStandardSelezionato ?: farmaco.dosaggio_standard)?.descrizione.orEmpty()
            }

            else -> {
                Toast.makeText(requireContext(), "Seleziona uno schema valido", Toast.LENGTH_SHORT).show()
                return
            }
        }

        binding.tvRisultatoValore.text = risultato
        binding.tvRisultatoUnita.text = unitaRisultato(farmaco)
        binding.tvRisultatoFrequenza.text = frequenzaRisultato(regola, farmaco)
        binding.tvAlertMessage.text = if (farmaco.alert.isEmpty()) {
            "Nessun alert disponibile per il farmaco selezionato."
        } else {
            farmaco.alert.joinToString(separator = "\n")
        }
    }

    private fun unitaRisultato(farmaco: Farmaco): String {
        return if (farmaco.unita_di_misura.contains("mg", ignoreCase = true)) {
            "mg"
        } else {
            farmaco.unita_di_misura
        }
    }

    private fun frequenzaRisultato(regola: RegolaCalcolo?, farmaco: Farmaco): String {
        return regola?.descrizione_dose
            ?: regola?.dose
            ?: dosaggioStandardSelezionato?.frequenza
            ?: farmaco.dosaggio_standard?.frequenza
            ?: "--"
    }

    private fun formattaNumero(numero: Double): String {
        return if (numero % 1.0 == 0.0) {
            numero.toInt().toString()
        } else {
            String.format("%.1f", numero)
        }
    }

    private data class PazienteDropdownItem(val paziente: Paziente) {
        override fun toString(): String = "${paziente.nome} ${paziente.cognome}"
    }

    private data class FarmacoDropdownItem(val farmaco: Farmaco) {
        override fun toString(): String = farmaco.nome_farmaco
    }

    private data class SchemaDropdownItem(
        val formato: Formato? = null,
        val regola: RegolaCalcolo? = null,
        val dosaggioStandard: DosaggioStandard? = null
    ) {
        override fun toString(): String {
            if (formato != null && regola != null) {
                val nomeFormato = formato.descrizione ?: formato.tipo
                return "$nomeFormato - ${regola.fascia.replace('_', ' ')}"
            }

            if (regola != null) {
                if (regola.descrizione_dose != null) {
                    return "${regola.fascia.replace('_', ' ')} - ${regola.descrizione_dose}"
                }

                if (regola.dose != null) {
                    return "${regola.fascia.replace('_', ' ')} - ${regola.dose}"
                }

                return regola.fascia.replace('_', ' ')
            }

            if (formato != null) {
                return formato.descrizione ?: formato.tipo
            }

            return dosaggioStandard?.let {
                "${it.descrizione} - ${it.frequenza}"
            } ?: ""
        }
    }
}
