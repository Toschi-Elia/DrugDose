package it.unisubria.drugdose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.unisubria.drugdose.databinding.FragmentPazientiBinding

class PazientiFragment : Fragment() {

    private var _binding: FragmentPazientiBinding?=null
    private val binding get()=_binding!!

    private lateinit var loadingDialog: LoadingDialog
    private val pazienteRepo= PatientRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPazientiBinding.inflate(inflater, container, false)
        return binding.root
       // return inflater.inflate(R.layout.fragment_pazienti, container, false)
    }
    override fun onViewCreated(view: View,savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        loadingDialog=LoadingDialog(requireActivity())
        binding.btnAggiungiPaziente.setOnClickListener{
            val bottomSheet= NuovoPazienteBottomSheet()
            bottomSheet.show(parentFragmentManager,"NuovoPazienteBottomSheet")
        }
    }



}
