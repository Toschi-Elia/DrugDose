package it.unisubria.drugdose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import it.unisubria.drugdose.models.CalcoloStorico

class StoricoViewModel : ViewModel() {
    private val repository = StoricoRepository()

    private val _listaCalcoli = MutableLiveData<List<CalcoloStorico>>(emptyList())
    val listaCalcoli: LiveData<List<CalcoloStorico>> get() = _listaCalcoli

    private val _errore = MutableLiveData<Int?>()
    val errore: LiveData<Int?> get() = _errore

    private val _caricamento = MutableLiveData<Boolean>()
    val caricamento: LiveData<Boolean> get() = _caricamento

    private var storicoListener: ListenerRegistration? = null

    init {
        ascoltaStorico()
    }

    fun salvaCalcolo(calcolo: CalcoloStorico) {
        repository.salvaCalcolo(calcolo) { successo, eccezione ->
            if (!successo || eccezione != null) {
                _errore.value = R.string.errore_sconosciuto_caricamento
            }
        }
    }

    fun ascoltaStorico() {
        if (storicoListener != null) {
            return
        }

        _caricamento.value = true
        storicoListener = repository.ascoltaStorico { calcoli, eccezione ->
            _caricamento.value = false
            if (eccezione != null) {
                _errore.value = R.string.errore_sconosciuto_caricamento
            } else {
                _errore.value = null
                _listaCalcoli.value = calcoli
            }
        }
    }

    override fun onCleared() {
        storicoListener?.remove()
        storicoListener = null
        super.onCleared()
    }
}
