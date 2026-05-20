package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.unisubria.drugdose.databinding.FragmentFarmaciBinding
import kotlinx.coroutines.launch

class FarmaciFragment : Fragment() {

    private var _binding: FragmentFarmaciBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FarmaciAdapter
    private lateinit var farmaciViewModel: FarmaciViewModel

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

        // 2. Osserva i farmaci caricati dal ViewModel condiviso
        viewLifecycleOwner.lifecycleScope.launch {
            farmaciViewModel.farmaci.collect { listaFarmaci ->
                adapter.aggiorna(listaFarmaci)
            }
        }

        // 3. Chiede il caricamento. Il ViewModel evita chiamate duplicate.
        farmaciViewModel.caricaFarmaci()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
