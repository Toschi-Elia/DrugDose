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
import it.unisubria.drugdose.models.DosaggioStandard
import it.unisubria.drugdose.models.Farmaco
import it.unisubria.drugdose.models.Formato
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var farmaciViewModel: FarmaciViewModel
    private var farmacoSelezionato: Farmaco? = null
    private var formatoSelezionato: Formato? = null
    private var dosaggioStandardSelezionato: DosaggioStandard? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        farmaciViewModel = ViewModelProvider(requireActivity())[FarmaciViewModel::class.java]

        val dropdownFarmaco = view.findViewById<AutoCompleteTextView>(R.id.dropdown_farmaco)
        val dropdownFormato = view.findViewById<AutoCompleteTextView>(R.id.dropdown_formato)

        viewLifecycleOwner.lifecycleScope.launch {
            farmaciViewModel.farmaci.collect { listaFarmaci ->
                val elementiFarmaco = listaFarmaci.map { FarmacoDropdownItem(it) }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    elementiFarmaco
                )
                dropdownFarmaco.setAdapter(adapter)
            }
        }

        dropdownFarmaco.setOnItemClickListener { _, _, position, _ ->
            val elemento = dropdownFarmaco.adapter.getItem(position) as? FarmacoDropdownItem
            farmacoSelezionato = elemento?.farmaco
            aggiornaDropdownFormati(dropdownFormato, farmacoSelezionato)
        }

        farmaciViewModel.caricaFarmaci()
    }

    private fun aggiornaDropdownFormati(
        dropdownFormato: AutoCompleteTextView,
        farmaco: Farmaco?
    ) {
        formatoSelezionato = null
        dosaggioStandardSelezionato = null

        val elementiDose = if (!farmaco?.formati.isNullOrEmpty()) {
            farmaco?.formati.orEmpty().map { DoseDropdownItem(formato = it) }
        } else {
            farmaco?.dosaggio_standard
                ?.let { listOf(DoseDropdownItem(dosaggioStandard = it)) }
                .orEmpty()
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            elementiDose
        )
        dropdownFormato.setAdapter(adapter)
        dropdownFormato.setText("", false)

        dropdownFormato.setOnItemClickListener { _, _, position, _ ->
            val elemento = dropdownFormato.adapter.getItem(position) as? DoseDropdownItem
            formatoSelezionato = elemento?.formato
            dosaggioStandardSelezionato = elemento?.dosaggioStandard
        }
    }

    private data class FarmacoDropdownItem(val farmaco: Farmaco) {
        override fun toString(): String = farmaco.nome_farmaco
    }

    private data class DoseDropdownItem(
        val formato: Formato? = null,
        val dosaggioStandard: DosaggioStandard? = null
    ) {
        override fun toString(): String {
            if (formato != null) {
                return formato.descrizione ?: formato.tipo
            }

            return dosaggioStandard?.let {
                "${it.descrizione} - ${it.frequenza}"
            } ?: ""
        }
    }
}
