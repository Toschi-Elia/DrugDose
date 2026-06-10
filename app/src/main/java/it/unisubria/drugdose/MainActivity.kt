package it.unisubria.drugdose

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import it.unisubria.drugdose.common.BaseActivity
import it.unisubria.drugdose.databinding.ActivityMainBinding
import it.unisubria.drugdose.ui.farmaci.FarmaciFragment
import it.unisubria.drugdose.ui.home.HomeFragment
import it.unisubria.drugdose.ui.pazienti.PazientiFragment

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
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
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()

            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
