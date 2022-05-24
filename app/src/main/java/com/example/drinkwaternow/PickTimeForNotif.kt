package com.example.drinkwaternow

import android.app.AlertDialog.THEME_HOLO_LIGHT
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import java.util.*

class PickTimeForNotif: AppCompatActivity(), TimePickerFragment.OnCompleteListener {

    val APP_PREFERENCES = "Settings"
    private val SAVED_FROM_HOUR = "SavedFromHour"
    private val SAVED_FROM_MINUTE = "SavedFromMinute"
    private val SAVED_TO_HOUR = "SavedToHour"
    private val SAVED_TO_MINUTE = "SavedToMinute"
    private val INTERVAL = "Interval"

    private var fromHour = 9
    private var fromMinute = 0
    private var toHour = 0
    private var toMinute = 0

    private var intervalHour = 99
    private var intervalMinute = 99
    private var intervalMinutes = 60

    private fun saveTimeToInternalStorage(prefName1: String, prefName2: String, Hour:Int, Minute:Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(prefName1, Hour)
            putInt(prefName2, Minute)
            apply()
        }
    }

    private fun saveIntervalToInternalStorage(interval:Int){
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(INTERVAL, interval)
            apply()
        }
    }

    private fun loadTimeFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
            fromHour = sharedPref.getInt(SAVED_FROM_HOUR, 9)
            fromMinute = sharedPref.getInt(SAVED_FROM_MINUTE, 0)
            toHour = sharedPref.getInt(SAVED_TO_HOUR, 0)
            toMinute = sharedPref.getInt(SAVED_TO_MINUTE, 0)
    }

    private fun loadIntervalFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        intervalMinutes = sharedPref.getInt(INTERVAL, 60)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_time_activity)
        val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTime)
        val toTimeEditText = findViewById<EditText>(R.id.editTextToTime)
        val intervalTextView = findViewById<TextView>(R.id.intervalTextView)
        val intervalChangeButton = findViewById<Button>(R.id.intervalChangeButton)

        //TODO сделать нормальный таймпикер, ограниченный по часам и минутам, кратным 5
        fun showIntervalTimePicker(view: View?) {
            val onTimeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    if ((selectedHour*60 +selectedMinute) < 180) {
                            intervalHour = selectedHour
                        if (selectedMinute % 5 == 0) {
                            intervalMinute = selectedMinute
                        }
                        else {
                            intervalMinute = 0
                            Toast.makeText(this, "Выберите число минут, кратное 5!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        Toast.makeText(this, "Выберите не более 3-х часов!", Toast.LENGTH_SHORT).show()
                        intervalHour = 1
                        intervalMinute = 0
                    }
                    intervalMinutes = (intervalHour*60 + intervalMinute)
                    intervalTextView!!.text = String.format(Locale.getDefault(), "уведомления через каждые %02d минут", intervalMinutes)
                    saveIntervalToInternalStorage(intervalMinutes)
                }

            val style = THEME_HOLO_LIGHT
            val timePickerDialog =
                TimePickerDialog(this,  style, onTimeSetListener, intervalHour, intervalMinute, true)
            timePickerDialog.setTitle("Выберите интервал")
            timePickerDialog.show()
        }

        loadTimeFromInternalStorage()
        loadIntervalFromInternalStorage()

        intervalTextView.text="уведомления через каждые $intervalMinutes минут"
        fromTimeEditText.setText("$fromHour : $fromMinute")
        toTimeEditText.setText("$toHour : $toMinute")

        fromTimeEditText.setOnClickListener{
            val bundle = Bundle()
            bundle.putInt("editText", 0)
            val newFragment: DialogFragment = TimePickerFragment()
            newFragment.arguments = bundle
            newFragment.show(supportFragmentManager, "timePicker")
            //showTimePickerDialog(it)
        }
        toTimeEditText.setOnClickListener{
            val bundle = Bundle()
            bundle.putInt("editText", 1)
            val newFragment: DialogFragment = TimePickerFragment()
            newFragment.arguments = bundle
            newFragment.show(supportFragmentManager, "timePicker")
            //showTimePickerDialog(it)
        }
        intervalChangeButton.setOnClickListener{
            showIntervalTimePicker(it)
        }
    }

    //вызывается после установки времени
    override fun onComplete(fragNumber: Int?, calendar: Calendar){
        if (fragNumber==0) {
            fromHour = calendar.get(Calendar.HOUR_OF_DAY)
            fromMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTime)
            fromTimeEditText.setText("$fromHour : $fromMinute")
            saveTimeToInternalStorage(SAVED_FROM_HOUR, SAVED_FROM_MINUTE, fromHour, fromMinute)
        }
        else if (fragNumber==1) {
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText2 = findViewById<EditText>(R.id.editTextToTime)
            fromTimeEditText2.setText("$toHour : $toMinute")
            saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
        }
    }
}