package it.unisubria.drugdose.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PreferitiRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun getPreferiti(onComplete: (MutableSet<String>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(mutableSetOf())

        db.collection("utenti").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val preferiti = document.get("farmaci_preferiti") as? List<String> ?: emptyList()
                    onComplete(preferiti.toMutableSet())
                } else {
                    onComplete(mutableSetOf())
                }
            }
            .addOnFailureListener {
                onComplete(mutableSetOf())
            }
    }

    fun aggiungiPreferito(idFarmaco: String) {
        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "farmaci_preferiti" to FieldValue.arrayUnion(idFarmaco)
        )

        db.collection("utenti").document(uid)
            .set(data, SetOptions.merge())
    }

    fun rimuoviPreferito(idFarmaco: String) {
        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "farmaci_preferiti" to FieldValue.arrayRemove(idFarmaco)
        )

        db.collection("utenti").document(uid)
            .set(data, SetOptions.merge())
    }
}