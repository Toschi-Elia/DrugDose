package it.unisubria.drugdose

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_pazienti -> {
                    replaceFragment(PazientiFragment())
                    true
                }
                R.id.nav_farmaci -> {
                    replaceFragment(FarmaciFragment())
                    true
                }
                else -> false
            }
        }

        testLetturaFirestore()
    }

    private fun testLetturaFirestore() {
        db.collection("farmaci")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("FirestoreTest", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("FirestoreTest", "Errore nel recupero dei documenti.", exception)
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}