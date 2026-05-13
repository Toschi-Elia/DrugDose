package it.unisubria.drugdose

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.unisubria.drugdose.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(){

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

         binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        var authRepo = AuthRepository()
        binding.btnAccediOspite.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        binding.btnRegistrati.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {

            Toast.makeText(this, "btn accedi", Toast.LENGTH_SHORT).show()
            binding.layoutEmail.error=null
            binding.layoutPassword.error=null

            val email = binding.tvEmail.text.toString().trim()
            val password = binding.tvPassword.text.toString().trim()

            var error=false

            if(email.isEmpty())
            {
                binding.layoutEmail.error=getString(R.string.error_email_empty)
                error=true
            }else if(email.isValidEmail())
            {
                binding.layoutEmail.error= getString(R.string.error_invalid_email)
            }

            if(password.isEmpty())
            {
                binding.layoutPassword.error=getString(R.string.error_password_empty)
                error=true
            }

            if(error)
                return@setOnClickListener




            authRepo.eseguiLogin(
                email,
                password
            ) { successo, errore ->
                if (successo) {
                    Toast.makeText(this, "Accesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Errore $errore", Toast.LENGTH_SHORT).show()
                    //TODO gestione errore
                }
            }

            binding.tvPasswordDimenticata.setOnClickListener {
                Toast.makeText(this, "tb password dimenticata", Toast.LENGTH_SHORT).show()
            }
        }


    }
}


