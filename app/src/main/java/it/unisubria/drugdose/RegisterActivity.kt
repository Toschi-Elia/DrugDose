package it.unisubria.drugdose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import it.unisubria.drugdose.databinding.ActivityRegisterBinding
import android.provider.Settings

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configuraInterfacciaBiometrica()

        loadingDialog = LoadingDialog(this)
        binding.textPassword.setOnEditorActionListener { _, actionId,_ ->
            if(actionId==EditorInfo.IME_ACTION_DONE)
            {
                binding.btnRegistrati.performClick()
                true
            }else false
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.registerRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var authRepo = AuthRepository()

        binding.btnAccediOspite.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val listTesti= listOf(binding.textNome to binding.layoutNome,
            binding.textCognome to binding.layoutCognome,
            binding.textMail to binding.layoutEmail,
            binding.textPassword to binding.layoutPassword)


        listTesti.forEach { (editText,textInputLayout)-> editText.doOnTextChanged { _,_,_,_ ->
            if(textInputLayout.error!=null)
                textInputLayout.error=null
        } }


        binding.btnRegistrati.setOnClickListener{

            binding.layoutNome.error = null
            binding.layoutCognome.error = null
            binding.layoutEmail.error = null
            binding.layoutPassword.error = null

            val nome = binding.textNome.text.toString().trim()
            val cognome = binding.textCognome.text.toString().trim()
            val mail = binding.textMail.text.toString().trim()
            val psw = binding.textPassword.text.toString()

            var error=false
            if(nome.isEmpty()) {
                error = true
                binding.layoutNome.error = getString(R.string.error_nome)
            }
            if(cognome.isEmpty())
            {
                error=true
                binding.layoutCognome.error =getString(R.string.error_cognome)
            }

            if(mail.isEmpty())
            {
                error=true
                binding.layoutEmail.error=getString(R.string.error_email_empty)
            }else if(!mail.isValidEmail())
            {
                error=true
                binding.layoutEmail.error=getString(R.string.error_invalid_email)
            }
            if(psw.isEmpty())
            {
                error=true
                binding.layoutPassword.error=getString(R.string.error_password_empty)
            }else if(!psw.isStrongPassword())
            {
                error=true
                binding.layoutPassword.error=getString(R.string.error_psw_not_strong)
            }

            if(error)
                return@setOnClickListener

            loadingDialog.mostraCaricamento()
            authRepo.registraUtente(nome,cognome, mail ,psw) { successo, errore ->
                loadingDialog.nascondiCaricamento()
                if (successo) {
                    val biometrica=binding.checkboxBiometric.isChecked

                    val sharedPref= getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)
                    sharedPref.edit().putBoolean("usa_biometria", biometrica).apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val msg= when(errore)
                    {
                        is FirebaseAuthUserCollisionException ->getString(R.string.error_firebase_mail_presente)
                        is FirebaseNetworkException -> getString(R.string.error_firebase_no_internet)
                        is UidNotFoundException ->getString(R.string.errore_uid_non_trovato)
                        else -> getString(R.string.error_generic_register)
                    }
                    binding.layoutEmail.error=msg
                }
            }
        }

        binding.btnAccedi.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()
        configuraInterfacciaBiometrica()
    }

    //todo insserisci risorse
    private fun configuraInterfacciaBiometrica() {
        val biometricManager = BiometricManager.from(this)
        val esitoControllo = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        // Reset del listener per evitare doppi click se la funzione viene richiamata
        binding.checkboxBiometric.setOnClickListener(null)
        val sharedPref = getSharedPreferences("ImpostazioniApp", Context.MODE_PRIVATE)

        when (esitoControllo) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Sensore OK e impronta registrata
                binding.checkboxBiometric.visibility = View.VISIBLE
                binding.checkboxBiometric.isEnabled = true
                binding.tvBiometricHelper.visibility = View.GONE

                // In registrazione parte deselezionata
                binding.checkboxBiometric.isChecked = false

                binding.checkboxBiometric.setOnClickListener {
                    sharedPref.edit().putBoolean("usa_biometria", binding.checkboxBiometric.isChecked).apply()
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Sensore OK ma nessuna impronta configurata nel sistema
                binding.checkboxBiometric.visibility = View.VISIBLE
                binding.checkboxBiometric.isEnabled = true
                binding.checkboxBiometric.isChecked = false
                binding.tvBiometricHelper.visibility = View.VISIBLE

                binding.checkboxBiometric.setOnClickListener {
                    binding.checkboxBiometric.isChecked = false // Impedisce la spunta

                    // Apre le impostazioni di sistema
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    }
                    startActivity(enrollIntent)
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // Hardware assente o rotto
                binding.checkboxBiometric.visibility = View.GONE
                binding.tvBiometricHelper.visibility = View.GONE
                sharedPref.edit().putBoolean("usa_biometria", false).apply()
            }
        }
    }

}