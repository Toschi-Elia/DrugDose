package it.unisubria.drugdose

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import it.unisubria.drugdose.models.Farmaco
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FarmaciViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _farmaci = MutableStateFlow<List<Farmaco>>(emptyList())
    val farmaci: StateFlow<List<Farmaco>> = _farmaci

    private val _caricamento = MutableStateFlow(false)
    val caricamento: StateFlow<Boolean> = _caricamento

    private val _errore = MutableStateFlow<String?>(null)
    val errore: StateFlow<String?> = _errore

    fun caricaFarmaci() {
        if (_farmaci.value.isNotEmpty() || _caricamento.value) {
            return
        }

        _caricamento.value = true
        _errore.value = null

        db.collection("farmaci")
            .get()
            .addOnSuccessListener { result ->
                val listaFarmaci = result.mapNotNull { document ->
                    try {
                        document.toObject(Farmaco::class.java)
                            .copy(id = document.id)
                    } catch (e: Exception) {
                        Log.w("FarmaciViewModel", "Errore nel parsing del documento ${document.id}", e)
                        null
                    }
                }

                _farmaci.value = listaFarmaci
                _caricamento.value = false
            }
            .addOnFailureListener { exception ->
                Log.w("FarmaciViewModel", "Errore nel caricamento dei farmaci.", exception)
                _errore.value = "Errore nel caricamento dei farmaci"
                _caricamento.value = false
            }
    }
}
