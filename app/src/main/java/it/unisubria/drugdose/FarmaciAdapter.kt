package it.unisubria.drugdose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import it.unisubria.drugdose.databinding.BottomSheetFarmacoBinding
import it.unisubria.drugdose.databinding.ItemFarmacoBinding
import it.unisubria.drugdose.models.Farmaco

class FarmaciAdapter(private var lista: List<Farmaco>) :
    RecyclerView.Adapter<FarmaciAdapter.FarmacoViewHolder>() {

    // ViewHolder con riferimenti alle view della card
    class FarmacoViewHolder(val binding: ItemFarmacoBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Crea il layout della card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmacoViewHolder {
        val binding = ItemFarmacoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FarmacoViewHolder(binding)
    }

    // Popola la card con i dati del farmaco
    override fun onBindViewHolder(holder: FarmacoViewHolder, position: Int) {
        val farmaco = lista[position]

        holder.binding.tvItemNomeFarmaco.text = farmaco.nome_farmaco.uppercase()
        holder.binding.tvItemPrincipioAttivo.text = farmaco.principio_attivo
        holder.binding.tvItemFormula.text =
            "Formula: ${farmaco.tipo_di_formula} - ${farmaco.unita_di_misura}"

        // Apre il BottomSheet con i dettagli al click
        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context
            val dialog = BottomSheetDialog(context)
            val sheetBinding = BottomSheetFarmacoBinding.inflate(LayoutInflater.from(context))

            // Popola il BottomSheet
            sheetBinding.bsNomeFarmaco.text = farmaco.nome_farmaco.uppercase()
            sheetBinding.bsPrincipioAttivo.text = farmaco.principio_attivo
            sheetBinding.bsIndicazione.text = farmaco.indicazione_clinica

            sheetBinding.bsTipoFormula.text = "Formula: ${farmaco.tipo_di_formula}"
            sheetBinding.bsUnitaMisura.text = "Unità di misura: ${farmaco.unita_di_misura}"
            sheetBinding.bsLimitazioni.text =
                "Età minima: ${farmaco.eta_minima} anni\nDurata max: ${farmaco.durata_massima}"

            if (farmaco.alert.isNotEmpty()) {
                sheetBinding.bsAlert.visibility = View.VISIBLE
                sheetBinding.bsAlert.text = farmaco.alert.joinToString("\n") { "- $it" }
            } else {
                sheetBinding.bsAlert.visibility = View.GONE
            }

            // Dosaggio standard
            if (farmaco.dosaggio_standard != null) {
                sheetBinding.bsDosaggioStandard.visibility = View.VISIBLE
                sheetBinding.bsDosaggioStandard.text =
                    "Dose: ${farmaco.dosaggio_standard.descrizione}\nFrequenza: ${farmaco.dosaggio_standard.frequenza}"
            } else {
                sheetBinding.bsDosaggioStandard.visibility = View.GONE
            }

            val regoleText = creaTestoRegole(farmaco)
            if (regoleText.isNotBlank()) {
                sheetBinding.bsRegoleCalcolo.visibility = View.VISIBLE
                sheetBinding.bsRegoleCalcolo.text = regoleText
            } else {
                sheetBinding.bsRegoleCalcolo.visibility = View.GONE
            }

            // Fonti
            if (farmaco.fonti.isNotEmpty()) {
                sheetBinding.bsFonti.visibility = View.VISIBLE
                sheetBinding.bsFonti.text = farmaco.fonti.joinToString("\n\n")
            } else {
                sheetBinding.bsFonti.visibility = View.GONE
            }

            dialog.setContentView(sheetBinding.root)
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

    private fun creaTestoRegole(farmaco: Farmaco): String {
        val testo = StringBuilder()

        if (!farmaco.regole_calcolo.isNullOrEmpty()) {
            testo.append("Regole di dosaggio\n")

            farmaco.regole_calcolo.forEach { regola ->
                testo.append("- ")
                testo.append(regola.fascia.replace('_', ' '))

                if (regola.descrizione_dose != null) {
                    testo.append(": ").append(regola.descrizione_dose)
                } else if (regola.dose != null) {
                    testo.append(": ").append(regola.dose)
                } else if (regola.dose_fissa != null) {
                    testo.append(": ").append(regola.dose_fissa).append(" mg")
                }

                testo.append("\n")
            }
        }

        farmaco.formati?.forEach { formato ->
            if (!formato.regole_calcolo.isNullOrEmpty()) {
                if (testo.isNotBlank()) {
                    testo.append("\n\n")
                }

                val titoloFormato = if (formato.descrizione != null) {
                    formato.descrizione
                } else {
                    formato.tipo
                }

                testo.append(titoloFormato).append("\n")

                formato.regole_calcolo.forEach { regola ->
                    testo.append("- ")
                    testo.append(regola.fascia.replace('_', ' '))

                    if (regola.descrizione_dose != null) {
                        testo.append(": ").append(regola.descrizione_dose)
                    } else if (regola.dose != null) {
                        testo.append(": ").append(regola.dose)
                    } else if (regola.dose_fissa != null) {
                        testo.append(": ").append(regola.dose_fissa).append(" mg")
                    }

                    testo.append("\n")
                }
            }
        }

        return testo.toString().trim()
    }
}
