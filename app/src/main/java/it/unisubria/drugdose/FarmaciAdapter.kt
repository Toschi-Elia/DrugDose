package it.unisubria.drugdose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import it.unisubria.drugdose.models.Farmaco

class FarmaciAdapter(private var lista: List<Farmaco>) :
    RecyclerView.Adapter<FarmaciAdapter.FarmacoViewHolder>() {

    // ViewHolder con riferimenti alle view della card
    class FarmacoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tv_item_nome_farmaco)
        val tvPrincipioAttivo: TextView = itemView.findViewById(R.id.tv_item_principio_attivo)
    }

    // Crea il layout della card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmacoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_farmaco, parent, false)
        return FarmacoViewHolder(view)
    }

    // Popola la card con i dati del farmaco
    override fun onBindViewHolder(holder: FarmacoViewHolder, position: Int) {
        val farmaco = lista[position]

        holder.tvNome.text = farmaco.nome_farmaco.uppercase()
        holder.tvPrincipioAttivo.text = farmaco.principio_attivo

        // Apre il BottomSheet con i dettagli al click
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val dialog = BottomSheetDialog(context)
            val sheetView = LayoutInflater.from(context)
                .inflate(R.layout.bottom_sheet_farmaco, null)

            // Popola il BottomSheet
            sheetView.findViewById<TextView>(R.id.bs_nome_farmaco).text =
                farmaco.nome_farmaco.uppercase()
            sheetView.findViewById<TextView>(R.id.bs_principio_attivo).text =
                farmaco.principio_attivo
            sheetView.findViewById<TextView>(R.id.bs_indicazione).text =
                farmaco.indicazione_clinica

            sheetView.findViewById<TextView>(R.id.bs_tipo_formula).text =
                "Formula: ${farmaco.tipo_di_formula}"
            sheetView.findViewById<TextView>(R.id.bs_unita_misura).text =
                "Unità di misura: ${farmaco.unita_di_misura}"
            
            sheetView.findViewById<TextView>(R.id.bs_limitazioni).text =
                "Età minima: ${farmaco.eta_minima} anni\nDurata max: ${farmaco.durata_massima}"
            
            val alertTextView = sheetView.findViewById<TextView>(R.id.bs_alert)
            if (farmaco.alert.isNotEmpty()) {
                alertTextView.visibility = View.VISIBLE
                alertTextView.text = farmaco.alert.joinToString("\n") { "- $it" }
            } else {
                alertTextView.visibility = View.GONE
            }

            // Dosaggio standard
            val dosaggioTextView = sheetView.findViewById<TextView>(R.id.bs_dosaggio_standard)
            if (farmaco.dosaggio_standard != null) {
                dosaggioTextView.visibility = View.VISIBLE
                dosaggioTextView.text = "Dose: ${farmaco.dosaggio_standard.descrizione}\nFrequenza: ${farmaco.dosaggio_standard.frequenza}"
            } else {
                dosaggioTextView.visibility = View.GONE
            }

            // Fonti
            val fontiTextView = sheetView.findViewById<TextView>(R.id.bs_fonti)
            if (farmaco.fonti.isNotEmpty()) {
                fontiTextView.visibility = View.VISIBLE
                fontiTextView.text = farmaco.fonti.joinToString("\n\n")
            } else {
                fontiTextView.visibility = View.GONE
            }

            dialog.setContentView(sheetView)
            dialog.show()
        }
    }

    // Restituisce il numero di elementi
    override fun getItemCount(): Int = lista.size

    // Aggiorna la lista e notifica i cambiamenti
    fun aggiorna(nuovaLista: List<Farmaco>) {
        lista = nuovaLista
        notifyDataSetChanged()
    }
}
