package it.unisubria.drugdose

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import it.unisubria.drugdose.databinding.BottomSheetNuovoPazienteBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

class NuovoPazienteBottomSheet : BottomSheetDialogFragment() {
     private var _binding: BottomSheetNuovoPazienteBinding? = null
    private val binding get() = _binding!!

    private val pazienteRepo = PatientRepository()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetNuovoPazienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())

        val patternLocale = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyyyy")

        val listTesti = listOf(
            binding.textNomePaziente to binding.layoutNomePaziente,
            binding.textCognomePaziente to binding.layoutCognomePaziente,
            binding.textDataNascita to binding.layoutDataNascita,
            binding.textPeso to binding.layoutPeso,
            binding.textAltezza to binding.layoutAltezza
        )

        listTesti.forEach { (editText, textInputLayout) ->
            editText.doOnTextChanged { _, _, _, _ ->
                if (textInputLayout.error != null)
                    textInputLayout.error = null
            }
        }

        binding.textCognomePaziente.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.textDataNascita.performClick()
                true
            } else {
                false
            }
        }

        binding.textAltezza.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSalvaPaziente.performClick()
                true
            } else {
                false
            }
        }

        binding.textDataNascita.setOnClickListener {
            val vincoli = com.google.android.material.datepicker.CalendarConstraints.Builder()
                .setValidator(com.google.android.material.datepicker.DateValidatorPointBackward.now())
                .build()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_picker_title))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(vincoli)
                .setTextInputFormat(java.text.SimpleDateFormat(patternLocale, Locale.getDefault()))
                .build()

            datePicker.addOnPositiveButtonClickListener { millisecondiScelti ->
                val formatoDataLocale = java.text.SimpleDateFormat(patternLocale, Locale.getDefault())
                val dataFormattata = formatoDataLocale.format(java.util.Date(millisecondiScelti))
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

        val nome = binding.textNomePaziente.text.toString().formattaMaiusc()
        val cognome = binding.textCognomePaziente.text.toString().formattaMaiusc()
        val dataNascitaStr = binding.textDataNascita.text.toString().trim()
        val pesoStr = binding.textPeso.text.toString().trim()
        val altezzaStr = binding.textAltezza.text.toString().trim()

        var error = false
        var dataNascitaValida: LocalDate? = null

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
                val patternLocale = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyyyy")
                val formatter = DateTimeFormatter.ofPattern(patternLocale, Locale.getDefault())

                val dataNascita = LocalDate.parse(dataNascitaStr, formatter)
                val oggi = LocalDate.now()
                val etaPaziente = ChronoUnit.YEARS.between(dataNascita, oggi)

                if (dataNascita.isAfter(oggi)) {
                    binding.layoutDataNascita.error = getString(R.string.error_data_futura)
                    error = true
                } else if (etaPaziente > 130) {
                    binding.layoutDataNascita.error = getString(R.string.error_eta_magg_130)
                    error = true
                } else {
                    dataNascitaValida = dataNascita
                }
            } catch (e: DateTimeParseException) {
                binding.layoutDataNascita.error = getString(R.string.error_formato_data_non_valido)
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

        if (error || dataNascitaValida == null) return

        val peso = pesoStr.toDoubleOrNull() ?: 0.0
        val altezza = altezzaStr.toIntOrNull() ?: 0
        val dataDaSalvare = dataNascitaValida.toString()
        val nuovoPaziente = Paziente(
            nome = nome,
            cognome = cognome,
            dataNascita = dataDaSalvare,
            peso = peso,
            altezza = altezza
        )
        loadingDialog.mostraCaricamento()

        pazienteRepo.aggiungiPaziente(nuovoPaziente) { successo, _ ->

            if (!isAdded || context == null) return@aggiungiPaziente

            loadingDialog.nascondiCaricamento()

            val mainActivity = activity as? MainActivity

            if (mainActivity != null) {
                val vistaPrincipale = mainActivity.binding.root
                val bottomNav = mainActivity.binding.bottomNavigation // Assicurati che l'ID sia questo!

                if (successo) {
                    val snackbar = Snackbar.make(
                        vistaPrincipale,
                        getString(R.string.paziente_salvato),
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.green))
                    snackbar.anchorView = bottomNav
                    snackbar.show()
                    dismiss()
                } else {
                    val snackbar = Snackbar.make(
                        vistaPrincipale,
                        getString(R.string.errore_salvataggio_paziente),
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error_red))
                    snackbar.anchorView = bottomNav
                    snackbar.show()
                }
            } else {
                if (successo) {
                    Toast.makeText(requireContext(), getString(R.string.paziente_salvato), Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}