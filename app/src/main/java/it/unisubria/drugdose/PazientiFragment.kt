package it.unisubria.drugdose

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import it.unisubria.drugdose.databinding.FragmentPazientiBinding
import android.app.Dialog
import it.unisubria.drugdose.databinding.DialogOspiteBinding

class PazientiFragment : Fragment() {

    private var _binding: FragmentPazientiBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PazientiViewModel
    private lateinit var pazienteAdapter: PazienteAdapter
    private var listaPazientiCompleta: List<Paziente> = emptyList()

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
            if (AuthRepository().isUtenteOspite()) {

                GestoreOspiti.mostraDialogRegistrazione(requireContext(), layoutInflater)

            } else {
                val bottomSheet = NuovoPazienteBottomSheet()
                bottomSheet.show(parentFragmentManager, "NuovoPaziente")
            }
        }

        binding.searchViewPazienti.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtraPazienti(query)
                binding.searchViewPazienti.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtraPazienti(newText)
                return true
            }
        })

        viewModel = ViewModelProvider(requireActivity())[PazientiViewModel::class.java]

        viewModel.listaPazienti.observe(viewLifecycleOwner) { pazienti ->
            listaPazientiCompleta = pazienti
            filtraPazienti(binding.searchViewPazienti.query?.toString())
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

    private fun filtraPazienti(query: String?) {
        val testoRicerca = query.orEmpty().trim()
        val pazientiFiltrati = if (testoRicerca.isBlank()) {
            listaPazientiCompleta
        } else {
            listaPazientiCompleta.filter { paziente ->
                val nomeCompleto = "${paziente.nome} ${paziente.cognome}"
                paziente.nome.contains(testoRicerca, ignoreCase = true) ||
                        paziente.cognome.contains(testoRicerca, ignoreCase = true) ||
                        nomeCompleto.contains(testoRicerca, ignoreCase = true)
            }
        }

        pazienteAdapter.aggiornaDati(pazientiFiltrati)
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
