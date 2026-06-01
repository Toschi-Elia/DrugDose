package it.unisubria.drugdose

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import it.unisubria.drugdose.calcolo.DoseCalcolata
import it.unisubria.drugdose.calcolo.DoseCalculator
import it.unisubria.drugdose.databinding.DialogSettingsBinding
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
        binding.btnImpostazioni.setOnClickListener { mostraDialogImpostazioni() }

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
        val erroreBloccante = validaVincoliCalcolo(farmaco, regolaSelezionata)
        if (erroreBloccante != null) {
            mostraErroreCalcolo(erroreBloccante)
            return
        }

        val risultato = DoseCalculator.calcolaDoseDaRegola(
            farmaco = farmaco,
            regola = regolaSelezionata,
            formato = formatoSelezionato,
            dosaggioStandard = dosaggioStandardSelezionato,
            pesoKg = pazientePeso
        )

        if (risultato == null) {
            Toast.makeText(requireContext(), "Seleziona uno schema valido", Toast.LENGTH_SHORT).show()
            return
        }

        mostraRisultato(risultato)
        binding.tvAlertMessage.text = if (farmaco.alert.isEmpty()) {
            "Nessun alert disponibile per il farmaco selezionato."
        } else {
            farmaco.alert.joinToString(separator = "\n")
        }
    }

    private fun validaVincoliCalcolo(farmaco: Farmaco, regola: RegolaCalcolo?): String? {
        val etaPaziente = pazienteDataNascita?.let { calcolaEtaDaDataNascita(it) }

        if (etaPaziente == null) {
            return "Calcolo non consentito: data di nascita del paziente non valida."
        }

        if (etaPaziente < farmaco.eta_minima) {
            return "Calcolo non consentito: farmaco non indicato sotto i ${farmaco.eta_minima} anni."
        }

        if (farmacoRichiedeRegola(farmaco) && regola == null) {
            return "Calcolo non consentito: seleziona uno schema valido."
        }

        if (regola != null) {
            val etaCompatibile = DoseCalculator.etaCompatibile(
                etaPaziente,
                regola.eta_min,
                regola.eta_max
            )
            if (!etaCompatibile) {
                return "Calcolo non consentito: età del paziente non compatibile con lo schema selezionato."
            }

            val pesoCompatibile = DoseCalculator.pesoCompatibile(
                pazientePeso,
                regola.peso_min_kg,
                regola.peso_max_kg
            )
            if (!pesoCompatibile) {
                return "Calcolo non consentito: peso del paziente non compatibile con lo schema selezionato."
            }
        }

        return null
    }

    private fun farmacoRichiedeRegola(farmaco: Farmaco): Boolean {
        val haRegoleFarmaco = !farmaco.regole_calcolo.isNullOrEmpty()
        val haRegoleFormato = farmaco.formati?.any { !it.regole_calcolo.isNullOrEmpty() } == true
        return haRegoleFarmaco || haRegoleFormato
    }

    private fun mostraErroreCalcolo(messaggio: String) {
        binding.tvRisultatoValore.text = "--"
        binding.tvRisultatoValore.textSize = 36f
        binding.tvRisultatoUnita.text = ""
        binding.tvRisultatoUnita.visibility = View.GONE
        binding.tvRisultatoFrequenza.text = "--"
        binding.tvAlertMessage.text = messaggio
    }

    private fun mostraRisultato(risultato: DoseCalcolata) {
        binding.tvRisultatoValore.text = risultato.valore
        binding.tvRisultatoValore.textSize = if (risultato.descrittivo) 20f else 36f
        binding.tvRisultatoUnita.text = risultato.unita.orEmpty()
        binding.tvRisultatoUnita.visibility = if (risultato.unita.isNullOrBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }
        binding.tvRisultatoFrequenza.text = risultato.frequenza
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
    private fun mostraDialogImpostazioni() {
        val binding = DialogSettingsBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lingueSupportate = mapOf(
            "it" to "Italiano",
            "en" to "English"
        )
        val linguaAttualeSalvata = "it"

        for ((codice, nome) in lingueSupportate) {
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = nome
                tag = codice
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_input_text_view_calcolatore))
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                if (codice == linguaAttualeSalvata) {
                    isChecked = true
                }
            }
            binding.rgLingua.addView(radioButton)
        }

        binding.rgLingua.setOnCheckedChangeListener { group, checkedId ->
            val bottoneSelezionato = group.findViewById<RadioButton>(checkedId)
            val nuovoCodiceLingua = bottoneSelezionato.tag as String
            println("Hai scelto la lingua: $nuovoCodiceLingua")
        }
        //tema
        val sharedPref = requireActivity().getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
        val currentThemeMode = sharedPref.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        binding.switchTemaScuro.isChecked = when (currentThemeMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                currentNightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }

        binding.switchTemaScuro.setOnCheckedChangeListener { _, isChecked ->
            val newMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            sharedPref.edit().putInt("theme_mode", newMode).apply()
            AppCompatDelegate.setDefaultNightMode(newMode)
            dialog.dismiss()
        }

        //LOG OUT
        binding.btnLogout.setOnClickListener {
            AuthRepository().eseguiLogout()
            dialog.dismiss()

            val intent = Intent(requireContext(), LoginActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        dialog.show()
    }}
