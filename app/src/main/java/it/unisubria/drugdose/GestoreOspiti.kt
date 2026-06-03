package it.unisubria.drugdose

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import it.unisubria.drugdose.RegisterActivity
import it.unisubria.drugdose.databinding.DialogOspiteBinding

object GestoreOspiti {

    fun mostraDialogRegistrazione(context: Context, layoutInflater: LayoutInflater) {
        val dialogBinding = DialogOspiteBinding.inflate(layoutInflater)

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialogBinding.btnDialogRegistrati.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }

        dialogBinding.btnDialogAnnulla.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}