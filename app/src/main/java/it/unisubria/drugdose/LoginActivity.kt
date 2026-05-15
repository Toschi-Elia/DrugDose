package it.unisubria.drugdose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import it.unisubria.drugdose.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        var authRepo = AuthRepository()
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
                    Toast.makeText(this, "Email di reset inviata a\n $email", Toast.LENGTH_SHORT).show()
                else
                {
                    val msg= when(eccezione)
                    {
                        is FirebaseAuthInvalidUserException ->getString(R.string.error_email_non_nel_db)
                        is FirebaseNetworkException -> getString(R.string.error_firebase_no_internet)
                        else ->getString(R.string.error_generic_psw_dimenticata)
                    }
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
                binding.tvPasswordDimenticata.setOnClickListener {
                    Toast.makeText(this, "tb password dimenticata", Toast.LENGTH_SHORT).show()
                }
            }


        }
    }
}


