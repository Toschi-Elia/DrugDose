package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import it.unisubria.drugdose.databinding.BottomSheetNuovoPazienteBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class NuovoPazienteBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetNuovoPazienteBinding? = null
    private val binding get() = _binding!!

    private val pazienteRepo = PatientRepository()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetNuovoPazienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())

        binding.textDataNascita.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_picker_title))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { millisecondiScelti ->
                val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN)
                val dataFormattata = formatoData.format(Date(millisecondiScelti))
                binding.textDataNascita.setText(dataFormattata)
            }

            datePicker.show(
                parentFragmentManager,
                getString(R.string.calendario_nascita)
            )
        }

        binding.btnSalvaPaziente.setOnClickListener { eseguiSalvataggio() }
    }

    private fun eseguiSalvataggio() {
        binding.layoutNomePaziente.error = null
        binding.layoutCognomePaziente.error = null
        binding.layoutAltezza.error = null
        binding.layoutDataNascita.error = null
        binding.layoutPeso.error = null

        val nome = binding.textNomePaziente.text.toString().trim()
        val cognome = binding.textCognomePaziente.text.toString().trim()
        val dataNascitaStr = binding.textDataNascita.text.toString().trim()
        val pesoStr = binding.textPeso.text.toString().trim()
        val altezzaStr = binding.textAltezza.text.toString().trim()

        var error = false

        if (nome.isEmpty()) {
            binding.layoutNomePaziente.error = getString(R.string.error_nome)
            error = true
        }

        if (cognome.isEmpty()) {
            binding.layoutCognomePaziente.error = getString(R.string.error_cognome)
            error = true
        }

        if (dataNascitaStr.isEmpty()) {
            binding.layoutDataNascita.error = getString(R.string.error_data_nascita_obl)
            error = true
        } else {
            try {
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val dataNascita = LocalDate.parse(dataNascitaStr, formatter)
                val oggi = LocalDate.now()
                val etaPaziente = ChronoUnit.YEARS.between(dataNascita, oggi)

                if (dataNascita.isAfter(oggi)) {
                    binding.layoutDataNascita.error = getString(R.string.error_data_futura)
                    error = true
                } else if (etaPaziente > 130) {
                    binding.layoutDataNascita.error = getString(R.string.error_eta_magg_130)
                    error = true
                }
            } catch (e: DateTimeParseException) {
                binding.layoutDataNascita.error = getString(R.string.formato_data_non_valido)
                error = true
            }
        }

        if (pesoStr.isEmpty()) {
            binding.layoutPeso.error = getString(R.string.error_peso_obl)
            error = true
        } else {
            val peso = pesoStr.toDoubleOrNull() ?: 0.0
            if (peso <= 0.5 || peso >= 650) {
                error = true
                binding.layoutPeso.error = getString(R.string.error_peso_non_valido)
            }
        }

        if (altezzaStr.isEmpty()) {
            binding.layoutAltezza.error = getString(R.string.error_altezza_obl)
            error = true
        } else {
            val altezza = altezzaStr.toIntOrNull() ?: 0
            if (altezza <= 20 || altezza >= 300) {
                error = true
                binding.layoutAltezza.error = getString(R.string.altezza_non_valida)
            }
        }

        if (error) return

        val peso = pesoStr.toDoubleOrNull() ?: 0.0
        val altezza = altezzaStr.toIntOrNull() ?: 0
        val nuovoPaziente = Paziente(nome, cognome, dataNascitaStr, peso, altezza)

        loadingDialog.mostraCaricamento()

        pazienteRepo.aggiungiPaziente(nuovoPaziente) { successo, _ ->
            loadingDialog.nascondiCaricamento()
            if (successo) {
                Toast.makeText(requireContext(), "Paziente salvato!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Errore nel salvataggio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}