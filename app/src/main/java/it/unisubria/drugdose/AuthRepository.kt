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

    fun recuperaPassword(email:String, onResult: (Boolean, kotlin.Exception?) -> Unit)
    {
        if(email.isEmpty())
        {
            onResult(false, Exception("Email vuota"))
            return
        }
        auth.useAppLanguage()
        auth.sendPasswordResetEmail(email).addOnCompleteListener {task ->
            if(task.isSuccessful)
                onResult(true,null)
            else
                onResult(false, task.exception)
        }
    }

    fun getUtenteAttuale() = auth.currentUser

    fun eseguiLogout() {
        auth.signOut()
    }

    fun accediComeOspite(onComplete: (Boolean, String?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.localizedMessage ?: "Errore durante l'accesso ospite")
                }
            }
    }

    fun isUtenteOspite(): Boolean {
        val utenteCorrente = auth.currentUser
        return utenteCorrente != null && utenteCorrente.isAnonymous
    }


}
