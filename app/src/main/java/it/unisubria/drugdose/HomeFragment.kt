package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
    private var farmacoSelezionato: Farmaco? = null
    private var formatoSelezionato: Formato? = null
    private var dosaggioStandardSelezionato: DosaggioStandard? = null
    private var regolaSelezionata: RegolaCalcolo? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        farmaciViewModel = ViewModelProvider(requireActivity())[FarmaciViewModel::class.java]

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

        binding.dropdownFarmaco.setOnItemClickListener { _, _, position, _ ->
            val elemento = binding.dropdownFarmaco.adapter.getItem(position) as? FarmacoDropdownItem
            farmacoSelezionato = elemento?.farmaco
            aggiornaDropdownSchemi(binding.dropdownFormato, farmacoSelezionato)
        }

        farmaciViewModel.caricaFarmaci()
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

        val elementiSchema = mutableListOf<SchemaDropdownItem>()

        if (farmaco != null) {
            if (!farmaco.regole_calcolo.isNullOrEmpty()) {
                farmaco.regole_calcolo.forEach { regola ->
                    elementiSchema.add(SchemaDropdownItem(regola = regola))
                }
            } else if (!farmaco.formati.isNullOrEmpty()) {
                farmaco.formati.forEach { formato ->
                    if (!formato.regole_calcolo.isNullOrEmpty()) {
                        formato.regole_calcolo.forEach { regola ->
                            elementiSchema.add(
                                SchemaDropdownItem(
                                    formato = formato,
                                    regola = regola
                                )
                            )
                        }
                    } else {
                        elementiSchema.add(SchemaDropdownItem(formato = formato))
                    }
                }
            } else if (farmaco.dosaggio_standard != null) {
                elementiSchema.add(
                    SchemaDropdownItem(
                        dosaggioStandard = farmaco.dosaggio_standard
                    )
                )
            }
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            elementiSchema
        )
        dropdownSchema.setAdapter(adapter)
        dropdownSchema.setText("", false)

        dropdownSchema.setOnItemClickListener { _, _, position, _ ->
            val elemento = dropdownSchema.adapter.getItem(position) as? SchemaDropdownItem
            formatoSelezionato = elemento?.formato
            dosaggioStandardSelezionato = elemento?.dosaggioStandard
            regolaSelezionata = elemento?.regola
        }
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
