package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import it.unisubria.drugdose.databinding.FragmentFarmaciBinding
import it.unisubria.drugdose.models.Farmaco
import it.unisubria.drugdose.repository.PreferitiRepository // Assicurati che l'import sia corretto
import kotlinx.coroutines.launch

class FarmaciFragment : Fragment() {

    private var _binding: FragmentFarmaciBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FarmaciAdapter
    private lateinit var farmaciViewModel: FarmaciViewModel

    private var listaFarmaciCompleta: List<Farmaco> = emptyList()
    private var preferitiLocali = mutableSetOf<String>()
    private var isFiltroAttivo = false

    private val preferitiRepo = PreferitiRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFarmaciBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        farmaciViewModel = ViewModelProvider(requireActivity())[FarmaciViewModel::class.java]

        adapter = FarmaciAdapter(emptyList(), preferitiLocali) { farmaco, isAggiunto ->
            if (isAggiunto) {
                preferitiRepo.aggiungiPreferito(farmaco.nome_farmaco)
            } else {
                preferitiRepo.rimuoviPreferito(farmaco.nome_farmaco)
                if (isFiltroAttivo) applicaFiltro()
            }
        }

        binding.recyclerFarmaci.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFarmaci.adapter = adapter

        binding.btnFiltroPreferiti.setOnClickListener {
            isFiltroAttivo = !isFiltroAttivo

            if (isFiltroAttivo) {
                binding.btnFiltroPreferiti.setImageResource(R.drawable.ic_star_filled)
                binding.btnFiltroPreferiti.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
            } else {
                binding.btnFiltroPreferiti.setImageResource(R.drawable.ic_star_outline)
                binding.btnFiltroPreferiti.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
            }

            applicaFiltro()
        }

        binding.searchViewFarmaci.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applicaFiltro()
                binding.searchViewFarmaci.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applicaFiltro()
                return true
            }
        })

        preferitiRepo.getPreferiti { preferitiScaricati ->
            preferitiLocali = preferitiScaricati

            viewLifecycleOwner.lifecycleScope.launch {
                farmaciViewModel.farmaci.collect { listaFarmaci ->
                    listaFarmaciCompleta = listaFarmaci
                    applicaFiltro()
                }
            }

            farmaciViewModel.caricaFarmaci()
        }
    }

    private fun applicaFiltro() {
        val queryTesto = binding.searchViewFarmaci.query?.toString().orEmpty().trim()

        val listaFiltrataPreferiti = if (isFiltroAttivo) {
            listaFarmaciCompleta.filter { preferitiLocali.contains(it.nome_farmaco) }
        } else {
            listaFarmaciCompleta
        }

        val listaFinale = if (queryTesto.isBlank()) {
            listaFiltrataPreferiti
        } else {
            listaFiltrataPreferiti.filter { farmaco ->
                farmaco.nome_farmaco.contains(queryTesto, ignoreCase = true)
            }
        }

        adapter.aggiornaDati(listaFinale, preferitiLocali)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}