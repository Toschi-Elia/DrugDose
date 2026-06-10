package it.unisubria.drugdose.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import it.unisubria.drugdose.models.CalcoloStorico

class StoricoRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getMedicoUid(): String? {
        return auth.currentUser?.uid
    }

    fun salvaCalcolo(
        calcolo: CalcoloStorico,
        onResult: (Boolean, Exception?) -> Unit
    ) {
        val medicoUid = getMedicoUid()

        if (medicoUid == null) {
            onResult(false, Exception("Nessun medico loggato. Impossibile salvare il calcolo"))
            return
        }

        if (calcolo.dataOra == null) {
            calcolo.dataOra = Timestamp.now()
        }

        db.collection("utenti").document(medicoUid)
            .collection("storico_calcoli")
            .add(calcolo)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { eccezione -> onResult(false, eccezione) }
    }

    fun ascoltaStorico(
        onResult: (List<CalcoloStorico>, Exception?) -> Unit
    ): ListenerRegistration? {
        val medicoUid = getMedicoUid()

        if (medicoUid == null) {
            onResult(emptyList(), Exception("Auth Error: Medico non autenticato"))
            return null
        }

        return db.collection("utenti").document(medicoUid)
            .collection("storico_calcoli")
            .orderBy("dataOra", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, errore ->
                if (errore != null) {
                    onResult(emptyList(), errore)
                    return@addSnapshotListener
                }

                val listaAggiornata = mutableListOf<CalcoloStorico>()

                if (snapshots != null) {
                    for (documento in snapshots.documents) {
                        val calcolo = documento.toObject<CalcoloStorico>()
                        if (calcolo != null) {
                            listaAggiornata.add(calcolo)
                        }
                    }
                }

                onResult(listaAggiornata, null)
            }
    }
}
