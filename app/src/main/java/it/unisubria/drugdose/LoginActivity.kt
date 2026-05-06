package it.unisubria.drugdose

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val buttonOspite= findViewById<Button>(R.id.btnAccediOspite)

        buttonOspite.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
        val bottoneRegistrati= findViewById<Button>(R.id.btnRegistrati)
        bottoneRegistrati.setOnClickListener{
            val intent= Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            Toast.makeText(this,"btn registrazione", Toast.LENGTH_SHORT).show()

        }

        val bottoneAccedi= findViewById<Button>(R.id.btn_login)

        bottoneAccedi.setOnClickListener{
            Toast.makeText(this,"btn accedi", Toast.LENGTH_SHORT).show()
        }

        val textPasswordDimenticata= findViewById<TextView>(R.id.tvPasswordDimenticata)
        textPasswordDimenticata.setOnClickListener {
            Toast.makeText(this, "tb password dimenticata", Toast.LENGTH_SHORT).show()
        }
    }


}


