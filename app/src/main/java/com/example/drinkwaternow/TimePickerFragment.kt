package com.example.drinkwaternow


import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*


class TimePickerFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener{

    interface OnCompleteListener {
        fun onComplete(fragNumber: Int?, calendar: Calendar)
    }

    private var mListener: OnCompleteListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)

        //интерфейс, позволяющий передать данные обратно в активность
        //val timeSetListener = activity as OnTimeSetListener?

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, R.style.TimePickerFragmentTheme, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = activity as OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnCompleteListener")
        }
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        val fragNumber = this.arguments?.getInt("editText")

        mListener?.onComplete(fragNumber, c)

    }
}