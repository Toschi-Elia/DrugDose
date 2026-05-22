package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.unisubria.drugdose.databinding.FragmentFarmaciBinding
import it.unisubria.drugdose.models.Farmaco
import kotlinx.coroutines.launch

class FarmaciFragment : Fragment() {

    private var _binding: FragmentFarmaciBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FarmaciAdapter
    private lateinit var farmaciViewModel: FarmaciViewModel
    private var listaFarmaciCompleta: List<Farmaco> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFarmaciBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        farmaciViewModel = ViewModelProvider(requireActivity())[FarmaciViewModel::class.java]

        // 1. Collega l'Adapter alla RecyclerView
        adapter = FarmaciAdapter(emptyList())
        binding.recyclerFarmaci.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFarmaci.adapter = adapter

        binding.searchViewFarmaci.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtraFarmaci(query)
                binding.searchViewFarmaci.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtraFarmaci(newText)
                return true
            }
        })

        // 2. Osserva i farmaci caricati dal ViewModel condiviso
        viewLifecycleOwner.lifecycleScope.launch {
            farmaciViewModel.farmaci.collect { listaFarmaci ->
                listaFarmaciCompleta = listaFarmaci
                filtraFarmaci(binding.searchViewFarmaci.query?.toString())
            }
        }

        // 3. Chiede il caricamento. Il ViewModel evita chiamate duplicate.
        farmaciViewModel.caricaFarmaci()
    }

    private fun filtraFarmaci(query: String?) {
        val testoRicerca = query.orEmpty().trim()
        val farmaciFiltrati = if (testoRicerca.isBlank()) {
            listaFarmaciCompleta
        } else {
            listaFarmaciCompleta.filter { farmaco ->
                farmaco.nome_farmaco.contains(testoRicerca, ignoreCase = true)
            }
        }

        adapter.aggiorna(farmaciFiltrati)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
