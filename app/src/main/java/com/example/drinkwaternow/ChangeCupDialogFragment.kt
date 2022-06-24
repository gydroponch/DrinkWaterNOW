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
        fun chosenCupSendInput(input: String)
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
        cupRecyclerView.adapter = ChangeCupAdapter(listOfCups, object: ChangeCupAdapter.OnCupClickListener{
            override fun onCupClick(input: String) {
                dataPasser.chosenCupSendInput(input)
            }
        })
    }
}



