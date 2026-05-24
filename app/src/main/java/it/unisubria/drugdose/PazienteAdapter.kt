package it.unisubria.drugdose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.unisubria.drugdose.databinding.ItemPazienteBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class PazienteAdapter(private val onPazienteClick:(Paziente)->Unit,private val onEliminaClick:(Paziente)-> Unit):
    RecyclerView.Adapter<PazienteAdapter.PazienteViewHolder>() {
    private var listaPazienti=listOf<Paziente>()

    fun aggionaDati(nuoviPazienti:List<Paziente>){
        listaPazienti=nuoviPazienti
        notifyDataSetChanged()
    }
    inner class PazienteViewHolder(val binding: ItemPazienteBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(paziente: Paziente)
        {
            binding.tvItemNomePaziente.text="${paziente.nome} ${paziente.cognome}"
            binding.tvItemPeso.text="Peso: ${paziente.peso} kg"
            binding.tvItemAltezza.text="Alt: ${paziente.altezza} cm"

            val (dataStr, etaStr) = formattaDataEta(paziente.dataNascita)
            binding.tvItemDataNascita.text = dataStr
            binding.tvItemEta.text = etaStr

            binding.btnEliminaPaziente.setOnClickListener { onEliminaClick(paziente) }
            binding.root.setOnClickListener { {onPazienteClick(paziente)} }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PazienteViewHolder {
        val binding = ItemPazienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PazienteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listaPazienti.size
    }

    override fun onBindViewHolder(holder: PazienteViewHolder, position: Int) {
        val pazienteCorrente = listaPazienti[position]
        holder.bind(pazienteCorrente)
    }
    private fun formattaDataEta(dataString: String): Pair<String, String> {
        return try {
            val dataNascita = LocalDate.parse(dataString)
            val eta = ChronoUnit.YEARS.between(dataNascita, LocalDate.now())

            // formato dinamico in base al paese (es. IT -> dd/MM/yyyy, US -> MM/dd/yyyy)
            val patternLocale = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyyyy")
            val formatter = DateTimeFormatter.ofPattern(patternLocale, Locale.getDefault())
            val dataFormattata = dataNascita.format(formatter)

            Pair("Data: $dataFormattata", "Età: $eta anni")

        } catch (e: Exception) {
            Pair("Data: N/D", "Età: N/D")
        }
    }
    fun aggiornaDati(nuoviPazienti: List<Paziente>) {
        listaPazienti = nuoviPazienti
        notifyDataSetChanged()
    }
}