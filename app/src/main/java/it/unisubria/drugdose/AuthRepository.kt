package it.unisubria.drugdose
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception


class UidNotFoundException: Exception("UID non generato da firebase")
class AuthRepository {

    private val auth= FirebaseAuth.getInstance()
    private val db= FirebaseFirestore.getInstance()

    fun registraUtente(nome: String, cognome: String, email: String, password: String, onResult: (Boolean, Exception?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId= task.result?.user?.uid
                    if(userId!=null)
                    {
                        val utenteMap=hashMapOf("nome" to nome, "cognome" to cognome, "email" to email)

                        //creazione il doc
                        db.collection("medici").document(userId).set(utenteMap).addOnSuccessListener{ onResult(true, null)}
                        .addOnFailureListener { eccezioneDb ->onResult(false, eccezioneDb) }
                    } else {
                        onResult(false, UidNotFoundException() )
                    }
                } else {
                    onResult(false, task.exception)
                }
            }
    }

    fun eseguiLogin(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // l'utente è già loggato
    fun getUtenteAttuale() = auth.currentUser

    fun eseguiLogout() {
        auth.signOut()
    }


}