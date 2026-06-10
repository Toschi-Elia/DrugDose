package it.unisubria.drugdose.ui.storico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.unisubria.drugdose.R
import it.unisubria.drugdose.databinding.ItemCalcoloStoricoBinding
import it.unisubria.drugdose.models.CalcoloStorico
import java.text.DateFormat

class StoricoAdapter : RecyclerView.Adapter<StoricoAdapter.StoricoViewHolder>() {
    private var listaCalcoli = listOf<CalcoloStorico>()

    class StoricoViewHolder(val binding: ItemCalcoloStoricoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoricoViewHolder {
        val binding = ItemCalcoloStoricoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoricoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoricoViewHolder, position: Int) {
        val calcolo = listaCalcoli[position]
        val context = holder.binding.root.context

        holder.binding.tvItemDataOra.text = formattaDataOra(calcolo)
        holder.binding.tvItemPaziente.text = calcolo.pazienteNome
        holder.binding.tvItemFarmaco.text = creaTestoFarmaco(calcolo)
        holder.binding.tvItemSchema.text = context.getString(
            R.string.storico_schema,
            calcolo.schema
        )
        holder.binding.tvItemDose.text = context.getString(
            R.string.storico_dose,
            creaTestoDose(calcolo)
        )
        holder.binding.tvItemFrequenza.text = context.getString(
            R.string.storico_frequenza,
            calcolo.frequenza
        )
    }

    override fun getItemCount(): Int {
        return listaCalcoli.size
    }

    fun aggiornaDati(nuoviCalcoli: List<CalcoloStorico>) {
        listaCalcoli = nuoviCalcoli
        notifyDataSetChanged()
    }

    private fun formattaDataOra(calcolo: CalcoloStorico): String {
        val data = calcolo.dataOra?.toDate() ?: return "--"
        val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        return formatter.format(data)
    }

    private fun creaTestoFarmaco(calcolo: CalcoloStorico): String {
        return if (calcolo.principioAttivo.isBlank()) {
            calcolo.farmacoNome
        } else {
            "${calcolo.farmacoNome} - ${calcolo.principioAttivo}"
        }
    }

    private fun creaTestoDose(calcolo: CalcoloStorico): String {
        return if (calcolo.doseUnita.isBlank()) {
            calcolo.doseValore
        } else {
            "${calcolo.doseValore} ${calcolo.doseUnita}"
        }
    }
}
