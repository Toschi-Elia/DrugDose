package it.unisubria.drugdose

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.sql.ClientInfoStatus

class PatientRepository {
    private val auth= FirebaseAuth.getInstance()
    private val db= FirebaseFirestore.getInstance()

    fun getMedicoUid(): String?{
        return auth.currentUser?.uid
    }

    fun aggiungiPaziente(paziente: Paziente, onResult:(Boolean, Exception?)->Unit){
        val medicoUid= getMedicoUid()

        if(medicoUid==null) {
            onResult(false, Exception("Nessun medico loggato. Impossibile salvare"))
            return
        }
        db.collection("utenti").document(medicoUid).collection("pazienti").add(paziente)
            .addOnSuccessListener { onResult(true,null) }.addOnFailureListener { eccezione->onResult(false,eccezione) }
    }
    fun ascoltaPazienti(onResult: (List<Paziente>, Exception?) -> Unit){
        db.collection("pazienti").orderBy("cognome").addSnapshotListener { snapshots, errore ->
            if(errore!=null) {
                onResult(emptyList(), errore)
                return@addSnapshotListener
            }
            val listaAggiornata=mutableListOf<Paziente>()
            if(snapshots!=null)
                for(documento in snapshots.documents)
                {
                    val paziente=documento.toObject<Paziente>()
                    if(paziente!=null)
                        listaAggiornata.add(paziente)
                }
            onResult(listaAggiornata,null)
        }
    }

    fun eliminaPaziente(pazienteId: String, onResult: (Boolean, Exception?) -> Unit) {
        if (pazienteId.isEmpty()) {
            onResult(false, Exception("ID paziente non valido"))
            return
        }

        db.collection("pazienti")
            .document(pazienteId) // Diciamo a Firebase ESATTAMENTE quale documento prendere
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e) }
    }

}