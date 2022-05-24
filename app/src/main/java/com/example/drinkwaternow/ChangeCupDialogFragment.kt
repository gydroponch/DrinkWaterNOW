package com.example.drinkwaternow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChangeCupDialogFragment(cupsList: List<Int>) : DialogFragment(){

    lateinit var dataPasser: StringListener
    private var listOfCups = cupsList

    interface StringListener{
        fun sendInput(input: String)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        dataPasser = context as StringListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.changecup_dialog_fragment, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cupRecyclerView: RecyclerView = view.findViewById(R.id.CupRecyclerView)
        cupRecyclerView.layoutManager = GridLayoutManager(context,3)
        cupRecyclerView.adapter = CustomAdapter(listOfCups, object: CustomAdapter.OnCupClickListener{
            override fun onCupClick(input: String) {
                dataPasser.sendInput(input)
            }
        })
    }
}

/*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return activity?.let{
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val changeCupView = inflater.inflate(R.layout.changecup_dialog_fragment, null)

        val cupRecyclerView: RecyclerView = changeCupView.findViewById(R.id.CupRecyclerView)
        cupRecyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        cupRecyclerView.adapter = CustomAdapter(fillList())
        cupRecyclerView.adapter
        builder.setTitle("Выберите кружку!")
        builder.setView(changeCupView)
        builder.create()
    } ?: throw IllegalStateException("Activity cannot be null")
}
*/

/*
class ChangeCupDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listener = DialogInterface.OnClickListener { _, which ->
            parentFragmentManager.setFragmentResult(REQUEST_KEY, bundleOf(KEY_RESPONSE to which))
        }
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setIcon(R.mipmap.ic_launcher_round)
            .setTitle("вилкой в глаза или в жопу раз")
            .setMessage("aaaaaaaaaaa")
            .setPositiveButton("yes", listener)
            .setNegativeButton("no", listener)
            .create()
    }

    companion object{
        @JvmStatic val TAG = ChangeCupDialogFragment::class.java.simpleName
        @JvmStatic val REQUEST_KEY ="$TAG:defaultRequestKey"
        @JvmStatic val KEY_RESPONSE = "RESPONSE"
    }
}
 */








