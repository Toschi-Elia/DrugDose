package it.unisubria.drugdose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import it.unisubria.drugdose.models.Farmaco

class FarmaciFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: FarmaciAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_farmaci, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Collega l'Adapter alla RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_farmaci)
        adapter = FarmaciAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 2. Scarica i farmaci da Firestore e aggiorna la lista
        caricaFarmaciDaFirestore()
    }

    private fun caricaFarmaciDaFirestore() {
        db.collection("farmaci")
            .get()
            .addOnSuccessListener { result ->
                val listaFarmaci = result.mapNotNull { document ->
                    try {
                        document.toObject(Farmaco::class.java)
                            .copy(id = document.id)
                    } catch (e: Exception) {
                        Log.w("FarmaciFragment", "Errore nel parsing del documento ${document.id}", e)
                        null
                    }
                }
                adapter.aggiorna(listaFarmaci)
            }
            .addOnFailureListener { exception ->
                Log.w("FarmaciFragment", "Errore nel caricamento dei farmaci.", exception)
            }
    }
}
