package it.unisubria.drugdose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import it.unisubria.drugdose.databinding.ActivityLoginBinding
import androidx.biometric.BiometricManager
import android.view.View
import android.view.inputmethod.EditorInfo

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingDialog: LoadingDialog
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        controllaSessioneEBiometria()
        configuraInterfacciaBiometrica()
        loadingDialog = LoadingDialog(this)
        var authRepo = AuthRepository()
        binding.textPassword.setOnEditorActionListener { _, actionId,_ ->
            if(actionId==EditorInfo.IME_ACTION_DONE)
            {
                binding.btnLogin.performClick()
                true
            }else false
        }
        binding.btnAccediOspite.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        binding.btnRegistrati.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //pulizia codice dopo errori
        val listTesti = listOf(
            binding.textEmail to binding.layoutEmail,
            binding.textPassword to binding.layoutPassword
        )


        listTesti.forEach { (editText, textInputLayout) ->
            editText.doOnTextChanged { _, _, _, _ ->
                if (textInputLayout.error != null)
                    textInputLayout.error = null
            }
        }

        binding.tvPasswordDimenticata.setOnClickListener {
            binding.layoutEmail.error=null
            binding.layoutPassword.error= null

            val email= binding.textEmail.text.toString().trim()

            var error=false
            if(email.isEmpty())
            {
                error=true
                binding.layoutEmail.error=getString(R.string.error_email_empty)
            }else if(!email.isValidEmail())
            {
                error=true
                binding.layoutEmail.error=getString(R.string.error_invalid_email)
            }

            if(error)
                return@setOnClickListener

            loadingDialog.mostraCaricamento()
            authRepo.recuperaPassword(email){
                successo,eccezione->
                loadingDialog.nascondiCaricamento()
                if(successo)
                    Toast.makeText(this,getString(R.string.email_di_reset_toast)+"\n $email", Toast.LENGTH_SHORT).show()
                else
                {
                    val msg= when(eccezione)
                    {
                        is FirebaseAuthInvalidUserException ->getString(R.string.error_email_non_nel_db)
                        is FirebaseNetworkException -> getString(R.string.error_firebase_no_internet)
                        else ->getString(R.string.error_generic_psw_dimenticata)

                    }
                    binding.layoutEmail.error = msg
                }
            }


        }


        binding.btnLogin.setOnClickListener {

            binding.layoutEmail.error = null
            binding.layoutPassword.error = null

            val email = binding.textEmail.text.toString().trim()
            val password = binding.textPassword.text.toString()

            var error = false

            if (email.isEmpty()) {
                binding.layoutEmail.error = getString(R.string.error_email_empty)
                error = true
            } else if (!email.isValidEmail()) {
                error = true
                binding.layoutEmail.error = getString(R.string.error_invalid_email)
            }

            if (password.isEmpty()) {
                binding.layoutPassword.error = getString(R.string.error_password_empty)
                error = true
            }

            if (error)
                return@setOnClickListener

            loadingDialog.mostraCaricamento()
            authRepo.eseguiLogin(
                email,
                password
            ) { successo, errore ->
                loadingDialog.nascondiCaricamento()
                if (successo) {
                    val biometrica=binding.checkboxBiometric.isChecked

                    val sharedPref= getSharedPreferences("ImpostazioniApp", android.content.Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("usa_biometria", biometrica).apply()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    when (errore) {

                        is FirebaseAuthInvalidUserException,
                        is FirebaseAuthInvalidCredentialsException -> {
                            val msgErrore = getString(R.string.error_firebase_credenziali_errate)
                            binding.layoutEmail.error = msgErrore
                            binding.layoutPassword.error = msgErrore
                        }

                        is FirebaseNetworkException -> {
                            binding.layoutEmail.error =
                                getString(R.string.error_firebase_no_internet)
                        }

                        else -> {
                            binding.layoutEmail.error = getString(R.string.error_generic_login)
                        }
                    }
                }

            }


        }

    }
    private fun configuraInterfacciaBiometrica() {
        val biometricManager = BiometricManager.from(this)
        val esitoControllo =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        when (esitoControllo) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // se il telefono ha il sensore ->mostra checkbox
                binding.checkboxBiometric.visibility = View.VISIBLE

                //seleziona la casella se già fatto prima
                val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
                binding.checkboxBiometric.isChecked = sharedPref.getBoolean("usa_biometria", false)
            }
            //todo da rivedere la politica di questo
//            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                // Il telefono HA il sensore, ma l'utente non ha mai configurato l'impronta
//                // nelle impostazioni del suo telefono. Meglio nascondere la checkbox.
//                binding.checkboxBiometric.visibility = View.GONE
//
//
//            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // Se non ha il sensore non lo mostra
                binding.checkboxBiometric.visibility = View.GONE


                val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
                sharedPref.edit().putBoolean("usa_biometria", false).apply()
            }

        }
    }
    private fun controllaSessioneEBiometria()
    {
        val utenteCorrente= auth.currentUser
        if(utenteCorrente!=null)
        {
            val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
            val usaBiometria = sharedPref.getBoolean("usa_biometria", false)
            if(usaBiometria)
                mostraPromtBiometrico()
            else
            {
                auth.signOut()
            }
        }
    }

    private fun mostraPromtBiometrico()
    {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // L'utente annulla. forziamo il logout
                auth.signOut()
                Toast.makeText(applicationContext,"Accesso biometrico annullato. Inserisci la password.",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                //impronta corretta
                Toast.makeText(applicationContext, "Bentornato in DrugDose", Toast.LENGTH_SHORT).show()
                goHome()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        })
        val promptInfo=BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.sblocco_biometric_title))
            .setSubtitle(getString(R.string.sblocco_biometric_subtitle))
            . setNegativeButtonText(getString(R.string.biometric_usa_psw))
            .build()
        //appare pop-up
        biometricPrompt.authenticate(promptInfo)
    }
    private fun goHome()
    {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}


