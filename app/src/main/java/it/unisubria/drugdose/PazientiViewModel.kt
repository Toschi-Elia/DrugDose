package it.unisubria.drugdose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PazientiViewModel: ViewModel() {
    private val repository = PatientRepository()

    private val _listaPazienti = MutableLiveData<List<Paziente>>()
    val listaPazienti: LiveData<List<Paziente>> get() = _listaPazienti

    // CORREZIONE QUI: Adesso entrambe le variabili sono Int?
    private val _errore = MutableLiveData<Int?>()
    val errore: LiveData<Int?> get() = _errore

    private val _caricamento = MutableLiveData<Boolean>()
    val caricamento: LiveData<Boolean> get() = _caricamento

    init {
        caricaPazienti()
    }

    private fun caricaPazienti() {
        _caricamento.value = true
        repository.ascoltaPazienti { pazienti, eccezione ->
            _caricamento.value = false
            if(eccezione != null) {
                // Ora funziona perché _errore accetta i numeri (Int)
                _errore.value = R.string.errore_sconosciuto_caricamento
            } else {
                _listaPazienti.value = pazienti
            }
        }
    }
    fun eliminaPaziente(paziente: Paziente) {
        _caricamento.value = true

        repository.eliminaPaziente(paziente.id) { successo, eccezione ->
            _caricamento.value = false

            if (!successo || eccezione != null) {
                _errore.value = R.string.errore_sconosciuto_caricamento
            }

        }
    }
}