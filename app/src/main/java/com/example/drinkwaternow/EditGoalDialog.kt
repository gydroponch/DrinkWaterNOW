package com.example.drinkwaternow
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class EditGoalDialog() : DialogFragment() {

    lateinit var dataPasser: StringListener
    var goalEditText = "2500"

    interface StringListener{
        fun editGoalSendInput(input: String)
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        dataPasser = context as StringListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val editGoalView = inflater.inflate(R.layout.edit_goal_dialog_fragment, null)
            val editGoalEditText = editGoalView.findViewById<EditText>(R.id.editGoalEditText)
            editGoalEditText.hint="2500"
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(editGoalView)
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                        goalEditText=editGoalEditText?.text.toString()
                        println("EDITTEXT__________: " +goalEditText)
                        dataPasser.editGoalSendInput(goalEditText)
                    })
                .setNegativeButton("Отмена",
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}






