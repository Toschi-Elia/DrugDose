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
import it.unisubria.drugdose.models.Farmaco
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var farmaciViewModel: FarmaciViewModel
    private var farmaciDisponibili: List<Farmaco> = emptyList()
    private var farmacoSelezionato: Farmaco? = null

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

        viewLifecycleOwner.lifecycleScope.launch {
            farmaciViewModel.farmaci.collect { listaFarmaci ->
                farmaciDisponibili = listaFarmaci

                val nomiFarmaci = listaFarmaci.map { it.nome_farmaco }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    nomiFarmaci
                )
                dropdownFarmaco.setAdapter(adapter)
            }
        }

        dropdownFarmaco.setOnItemClickListener { _, _, position, _ ->
            farmacoSelezionato = farmaciDisponibili.getOrNull(position)
        }

        farmaciViewModel.caricaFarmaci()
    }
}
