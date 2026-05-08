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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var authRepo = AuthRepository()
        val bottoneOspite = findViewById<Button>(R.id.btnAccediOspite)
        val bottoneAccedi= findViewById<Button>(R.id.btnAccedi)
        val bottoneRegistrati= findViewById<Button>(R.id.btn_registrati)

        val textNome= findViewById<TextView>(R.id.text_nome).text.toString()
        val textCognome= findViewById<TextView>(R.id.text_cognome).text.toString()




        bottoneOspite.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        bottoneRegistrati.setOnClickListener{
            val textMail= findViewById<TextView>(R.id.text_mail)
            val textPsw= findViewById<TextView>(R.id.text_password)
            authRepo.registraUtente(textMail.text.toString(),textPsw.text.toString()){
                successo, errore -> if(successo) {
                    Toast.makeText(this, "Registrazione effettuata", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                }else{
                    Toast.makeText(this, "Errore $errore",Toast.LENGTH_SHORT).show()
                    textMail.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                }
            }

        }

        bottoneAccedi.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }
    fun verificaPassword(str: String): Boolean {

return true
    }


}