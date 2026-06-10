package it.unisubria.drugdose.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import it.unisubria.drugdose.MainActivity
import it.unisubria.drugdose.R
import it.unisubria.drugdose.common.BaseActivity
import it.unisubria.drugdose.common.LoadingDialog
import it.unisubria.drugdose.databinding.ActivityLoginBinding
import it.unisubria.drugdose.repository.AuthRepository
import it.unisubria.drugdose.util.isValidEmail

class LoginActivity : BaseActivity() {

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
            authRepo.accediComeOspite { successo, errore ->
                loadingDialog.nascondiCaricamento()

                if (successo) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, errore, Toast.LENGTH_LONG).show()
                }
            }


        }

        binding.btnRegistrati.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

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

            val email= binding.textEmail.text.toString().lowercase().trim()

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
    override fun onResume(){
        super.onResume()
        configuraInterfacciaBiometrica()
    }
    private fun configuraInterfacciaBiometrica() {
        val biometricManager = BiometricManager.from(this)
        val esitoControllo =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        binding.checkboxBiometric.setOnClickListener(null)

        when (esitoControllo) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.checkboxBiometric.visibility = View.VISIBLE
                binding.checkboxBiometric.isEnabled = true
                binding.tvBiometricHelper.visibility = View.GONE

                val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
                binding.checkboxBiometric.isChecked = sharedPref.getBoolean("usa_biometria", false)

                binding.checkboxBiometric.setOnClickListener {
                    val isChecked = binding.checkboxBiometric.isChecked
                    sharedPref.edit().putBoolean("usa_biometria", isChecked).apply()
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                binding.checkboxBiometric.visibility = View.VISIBLE
                binding.checkboxBiometric.isEnabled = true
                binding.checkboxBiometric.isChecked = false

                binding.tvBiometricHelper.visibility = View.VISIBLE

                binding.checkboxBiometric.setOnClickListener {
                    binding.checkboxBiometric.isChecked = false

                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    }
                    startActivity(enrollIntent)

                }
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                binding.checkboxBiometric.visibility = View.GONE
                binding.tvBiometricHelper.visibility = View.GONE

                val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
                sharedPref.edit().putBoolean("usa_biometria", false).apply()
            }
        }
    }
    private fun controllaSessioneEBiometria() {
        val utenteCorrente = auth.currentUser

        if (utenteCorrente != null) {
            if (utenteCorrente.isAnonymous) {
                auth.signOut()
            } else {

                val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
                val usaBiometria = sharedPref.getBoolean("usa_biometria", false)

                if (usaBiometria) {
                    mostraPromtBiometrico()
                } else {
                    auth.signOut()
                }
            }
        }
    }

    private fun mostraPromtBiometrico()
    {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                auth.signOut()
                Toast.makeText(applicationContext,"Accesso biometrico annullato. Inserisci la password.",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
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
        biometricPrompt.authenticate(promptInfo)
    }
    private fun goHome()
    {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
