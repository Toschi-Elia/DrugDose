package it.unisubria.drugdose

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import it.unisubria.drugdose.databinding.BottomSheetFarmacoBinding
import it.unisubria.drugdose.databinding.ItemFarmacoBinding
import it.unisubria.drugdose.models.Farmaco
import java.util.Locale

class FarmaciAdapter(
    private var lista: List<Farmaco>,
    private var preferitiSet: MutableSet<String>,
    private val onStellaClick: (Farmaco, Boolean) -> Unit
) : RecyclerView.Adapter<FarmaciAdapter.FarmacoViewHolder>() {

    class FarmacoViewHolder(val binding: ItemFarmacoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FarmacoViewHolder {
        val binding = ItemFarmacoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FarmacoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FarmacoViewHolder, position: Int) {
        val farmaco = lista[position]
        val context = holder.binding.root.context
        val languageCode = codiceLinguaCorrente(context)

        holder.binding.tvItemNomeFarmaco.text = farmaco.nome_farmaco.uppercase()
        holder.binding.tvItemPrincipioAttivo.text = farmaco.principioAttivoLocalizzato(languageCode)
        holder.binding.tvItemFormula.text = context.getString(
            R.string.item_farmaco_formula,
            farmaco.tipoFormulaLocalizzato(languageCode),
            farmaco.unitaMisuraLocalizzata(languageCode)
        )

        val isPreferito = preferitiSet.contains(farmaco.nome_farmaco)

        if (isPreferito) {
            holder.binding.btnStellaPreferito.setImageResource(R.drawable.ic_star_filled)
        } else {
            holder.binding.btnStellaPreferito.setImageResource(R.drawable.ic_star_outline)
        }

        holder.binding.btnStellaPreferito.setOnClickListener {
            val diventeraPreferito = !isPreferito

            if (diventeraPreferito) {
                preferitiSet.add(farmaco.nome_farmaco)
            } else {
                preferitiSet.remove(farmaco.nome_farmaco)
            }
            notifyItemChanged(position)

            onStellaClick(farmaco, diventeraPreferito)
        }

        holder.binding.root.setOnClickListener {
            val dialog = BottomSheetDialog(context)
            val sheetBinding = BottomSheetFarmacoBinding.inflate(LayoutInflater.from(context))

            sheetBinding.bsNomeFarmaco.text = farmaco.nome_farmaco.uppercase()
            sheetBinding.bsPrincipioAttivo.text = farmaco.principioAttivoLocalizzato(languageCode)
            sheetBinding.bsIndicazione.text = farmaco.indicazioneClinicaLocalizzata(languageCode)

            sheetBinding.bsTipoFormula.text = context.getString(
                R.string.bs_farmaco_formula,
                farmaco.tipoFormulaLocalizzato(languageCode)
            )
            sheetBinding.bsUnitaMisura.text = context.getString(
                R.string.bs_unita_misura_dettaglio,
                farmaco.unitaMisuraLocalizzata(languageCode)
            )

            sheetBinding.bsLimitazioni.text = context.getString(
                R.string.bs_limitazioni_testo,
                farmaco.eta_minima.toString(),
                farmaco.durataMassimaLocalizzata(languageCode)
            )

            val alert = farmaco.alertLocalizzati(languageCode)
            if (alert.isNotEmpty()) {
                sheetBinding.bsAlert.visibility = View.VISIBLE
                sheetBinding.bsAlert.text = alert.joinToString("\n") { "- $it" }
            } else {
                sheetBinding.bsAlert.visibility = View.GONE
            }

            if (farmaco.dosaggio_standard != null) {
                sheetBinding.bsDosaggioStandard.visibility = View.VISIBLE
                sheetBinding.bsDosaggioStandard.text = context.getString(
                    R.string.bs_dosaggio_standard_dettaglio,
                    farmaco.dosaggio_standard.descrizioneLocalizzata(languageCode),
                    farmaco.dosaggio_standard.frequenzaLocalizzata(languageCode)
                )
            } else {
                sheetBinding.bsDosaggioStandard.visibility = View.GONE
            }

            val regoleText = creaTestoRegole(context, farmaco, languageCode)
            if (regoleText.isNotBlank()) {
                sheetBinding.bsRegoleCalcolo.visibility = View.VISIBLE
                sheetBinding.bsRegoleCalcolo.text = regoleText
            } else {
                sheetBinding.bsRegoleCalcolo.visibility = View.GONE
            }

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

    override fun getItemCount(): Int = lista.size

    fun aggiornaDati(nuovaLista: List<Farmaco>, nuoviPreferiti: MutableSet<String>) {
        lista = nuovaLista
        preferitiSet = nuoviPreferiti
        notifyDataSetChanged()
    }

    private fun creaTestoRegole(context: Context, farmaco: Farmaco, languageCode: String): String {
        val testo = StringBuilder()

        if (!farmaco.regole_calcolo.isNullOrEmpty()) {
            testo.append(context.getString(R.string.regole_dosaggio_titolo)).append("\n")

            farmaco.regole_calcolo.forEach { regola ->
                testo.append("- ")
                testo.append(regola.fasciaLocalizzata(languageCode))

                if (regola.descrizioneDoseLocalizzata(languageCode) != null) {
                    testo.append(": ").append(regola.descrizioneDoseLocalizzata(languageCode))
                } else if (regola.doseLocalizzata(languageCode) != null) {
                    testo.append(": ").append(regola.doseLocalizzata(languageCode))
                } else if (regola.dose_fissa != null) {
                    testo.append(": ").append(context.getString(R.string.dose_fissa_mg, regola.dose_fissa.toString()))
                }

                testo.append("\n")
            }
        }

        farmaco.formati?.forEach { formato ->
            if (!formato.regole_calcolo.isNullOrEmpty()) {
                if (testo.isNotBlank()) {
                    testo.append("\n\n")
                }

                val titoloFormato = if (formato.descrizioneLocalizzata(languageCode) != null) {
                    formato.descrizioneLocalizzata(languageCode)
                } else {
                    formato.tipoLocalizzato(languageCode)
                }

                testo.append(titoloFormato).append("\n")

                formato.regole_calcolo.forEach { regola ->
                    testo.append("- ")
                    testo.append(regola.fasciaLocalizzata(languageCode))

                    if (regola.descrizioneDoseLocalizzata(languageCode) != null) {
                        testo.append(": ").append(regola.descrizioneDoseLocalizzata(languageCode))
                    } else if (regola.doseLocalizzata(languageCode) != null) {
                        testo.append(": ").append(regola.doseLocalizzata(languageCode))
                    } else if (regola.dose_fissa != null) {
                        testo.append(": ").append(context.getString(R.string.dose_fissa_mg, regola.dose_fissa.toString()))
                    }

                    testo.append("\n")
                }
            }
        }

        return testo.toString().trim()
    }

    private fun codiceLinguaCorrente(context: Context): String {
        val locales = context.resources.configuration.locales
        return locales.get(0)?.language ?: Locale.getDefault().language
    }
}
