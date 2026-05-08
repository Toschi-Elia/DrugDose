package it.unisubria.drugdose

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bottoneOspite = findViewById<Button>(R.id.btnAccediOspite)
        val bottoneRegistrati = findViewById<Button>(R.id.btnRegistrati)
        val textPasswordDimenticata = findViewById<TextView>(R.id.tvPasswordDimenticata)
        val bottoneAccedi = findViewById<Button>(R.id.btn_login)

        var authRepo = AuthRepository()
        bottoneOspite.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }

        bottoneRegistrati.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        bottoneAccedi.setOnClickListener {
            Toast.makeText(this, "btn accedi", Toast.LENGTH_SHORT).show()
            val textMail = findViewById<TextView>(R.id.text_mail)
            val textPsw = findViewById<TextView>(R.id.text_password)
            authRepo.eseguiLogin(
                textMail.text.toString(),
                textPsw.text.toString()
            ) { successo, errore ->
                if (successo) {
                    Toast.makeText(this, "Accesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this, "Errore $errore", Toast.LENGTH_SHORT).show()
                    //TODO aggiunta cambio colore
                }
            }

            textPasswordDimenticata.setOnClickListener {
                Toast.makeText(this, "tb password dimenticata", Toast.LENGTH_SHORT).show()
            }
        }


    }
}


