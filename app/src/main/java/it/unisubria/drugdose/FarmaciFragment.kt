package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class FarmaciFragment : Fragment() {

    private lateinit var adapter: FarmaciAdapter
    private lateinit var farmaciViewModel: FarmaciViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_farmaci, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        farmaciViewModel = ViewModelProvider(requireActivity())[FarmaciViewModel::class.java]

        // 1. Collega l'Adapter alla RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_farmaci)
        adapter = FarmaciAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 2. Osserva i farmaci caricati dal ViewModel condiviso
        viewLifecycleOwner.lifecycleScope.launch {
            farmaciViewModel.farmaci.collect { listaFarmaci ->
                adapter.aggiorna(listaFarmaci)
            }
        }

        // 3. Chiede il caricamento. Il ViewModel evita chiamate duplicate.
        farmaciViewModel.caricaFarmaci()
    }
}
