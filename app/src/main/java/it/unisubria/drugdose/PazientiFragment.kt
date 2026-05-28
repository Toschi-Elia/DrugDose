package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import it.unisubria.drugdose.databinding.FragmentPazientiBinding

class PazientiFragment : Fragment() {

    private var _binding: FragmentPazientiBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PazientiViewModel
    private lateinit var pazienteAdapter: PazienteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPazientiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pazienteAdapter = PazienteAdapter(
            onPazienteClick = { paziente ->
                val bundle = Bundle().apply {
                    putString("ID_PAZIENTE", paziente.id)
                    putString("NOME_PAZIENTE", "${paziente.nome} ${paziente.cognome}")
                    putString("DATA_NASCITA_PAZIENTE", paziente.dataNascita)
                    putDouble("PESO_PAZIENTE", paziente.peso)
                    putInt("ALTEZZA_PAZIENTE", paziente.altezza)
                }
                val homeFragment= HomeFragment()
                homeFragment.arguments=bundle

                parentFragmentManager.beginTransaction().replace(R.id.fragment_container,homeFragment).addToBackStack(null).commit()

            },
            onEliminaClick = { paziente ->
                mostraDialogConfermaEliminazione(paziente)
            }
        )

        binding.rvPazienti.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pazienteAdapter
        }

        binding.btnAggiungiPaziente.setOnClickListener {
            val bottomSheet = NuovoPazienteBottomSheet()
            bottomSheet.show(parentFragmentManager, "NuovoPazienteBottomSheet")
        }

        viewModel = ViewModelProvider(this)[PazientiViewModel::class.java]

        viewModel.listaPazienti.observe(viewLifecycleOwner) { pazienti ->
            pazienteAdapter.aggiornaDati(pazienti)
        }

        viewModel.caricamento.observe(viewLifecycleOwner) { inCaricamento ->
            binding.progressBarPazienti.visibility = if (inCaricamento) View.VISIBLE else View.GONE
        }

        viewModel.errore.observe(viewLifecycleOwner) { idStringaErrore ->
            if (idStringaErrore != null) {
                val mainActivity = activity as? MainActivity
                val vistaPrincipale = mainActivity?.binding?.root ?: binding.root
                val bottomNav = mainActivity?.binding?.bottomNavigation

                val snackbar = Snackbar.make(vistaPrincipale, getString(idStringaErrore), Snackbar.LENGTH_LONG)
                snackbar.setBackgroundTint(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.error_red
                    )
                )

                bottomNav?.let { snackbar.anchorView = it }
                snackbar.show()
            }
        }
    }
    private fun mostraDialogConfermaEliminazione(paziente: Paziente) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Elimina Paziente")
            .setMessage("Sei sicuro di voler eliminare definitivamente il paziente ${paziente.nome} ${paziente.cognome}?")
            .setNegativeButton("Annulla", null)
            .setPositiveButton("Elimina") { _, _ ->
                viewModel.eliminaPaziente(paziente)

            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
