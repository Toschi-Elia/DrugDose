package it.unisubria.drugdose

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import it.unisubria.drugdose.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


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

        binding.btnRegistrati.setOnClickListener{
            val nome = binding.textNome.text.toString()
            val cognome = binding.textCognome.text.toString()
            val mail = binding.textMail.text.toString()
            val psw = binding.textPassword.text.toString()

            var error=false
            if(nome.isEmpty()) {
                error = true
                binding.layoutNome.error = getString(R.string.error_nome)
            }

        //todo gestione errori



            authRepo.registraUtente(mail, psw) { successo, errore ->
                if (successo) {
                    Toast.makeText(this, "Registrazione effettuata", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Errore $errore", Toast.LENGTH_SHORT).show()
                    binding.textMail.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                }
            }


        }

        binding.btnAccedi.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }
    fun verificaPassword(str: String): Boolean {

        return true
    }


}