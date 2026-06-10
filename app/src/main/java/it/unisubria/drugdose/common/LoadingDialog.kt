package it.unisubria.drugdose.common
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.ProgressBar
import android.widget.RelativeLayout


class LoadingDialog (private val context:Context){
    private var dialog: Dialog?=null

    fun mostraCaricamento()
    {
        if(dialog?.isShowing==true)
            return

        dialog=Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            setCancelable(false)

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            var progressBar= ProgressBar(context)

            val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            setContentView(progressBar,params)
        }
        dialog?.show()
    }

    fun nascondiCaricamento()
    {
        if(dialog?.isShowing==true)
            dialog?.dismiss()
    }
}
