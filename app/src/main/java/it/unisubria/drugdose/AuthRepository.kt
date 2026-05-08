package it.unisubria.drugdose
import com.google.firebase.auth.FirebaseAuth


class AuthRepository {

    private val auth= FirebaseAuth.getInstance()
    fun registraUtente(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
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