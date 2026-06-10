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


    inner class PazienteViewHolder(val binding: ItemPazienteBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(paziente: Paziente) {
            val context = itemView.context

            binding.tvItemNomePaziente.text = "${paziente.nome.formattaMaiusc()} ${paziente.cognome.formattaMaiusc()}"

            binding.tvItemPeso.text = context.getString(R.string.item_peso, paziente.peso.toString())
            binding.tvItemAltezza.text = context.getString(R.string.item_altezza, paziente.altezza.toString())

            val (dataStr, etaStr) = formattaDataEta(paziente.dataNascita)
            binding.tvItemDataNascita.text = context.getString(R.string.item_data, dataStr)
            binding.tvItemEta.text = context.getString(R.string.item_eta, etaStr)

            binding.btnEliminaPaziente.setOnClickListener { onEliminaClick(paziente) }
            binding.root.setOnClickListener { onPazienteClick(paziente) }
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

            val patternLocale = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyyyy")
            val formatter = DateTimeFormatter.ofPattern(patternLocale, Locale.getDefault())
            val dataFormattata = dataNascita.format(formatter)

            Pair(dataFormattata, eta.toString())

        } catch (e: Exception) {
            Pair("-", "-")
        }
    }
    fun aggiornaDati(nuoviPazienti: List<Paziente>) {
        listaPazienti = nuoviPazienti
        notifyDataSetChanged()
    }
}
