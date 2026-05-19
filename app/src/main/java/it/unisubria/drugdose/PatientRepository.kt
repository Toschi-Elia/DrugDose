package it.unisubria.drugdose

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

}