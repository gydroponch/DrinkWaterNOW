package com.example.drinkwaternow

import android.app.AlarmManager
import android.app.AlertDialog.THEME_HOLO_LIGHT
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
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
    private var toHour = 23
    private var toMinute = 0

    private var intervalHour = 99
    private var intervalMinute = 99
    private var intervalMinutes = 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_time_activity)
        val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTime)
        val toTimeEditText = findViewById<EditText>(R.id.editTextToTime)
        val intervalTextView = findViewById<TextView>(R.id.intervalTextView)
        val intervalChangeButton = findViewById<Button>(R.id.intervalChangeButton)

        //TODO сделать нормальный таймпикер интервала уведомлений, ограниченный по часам и минутам, кратным 5
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
                    saveTimeIntervalToInternalStorage(intervalMinutes)
                }

            val style = THEME_HOLO_LIGHT
            val timePickerDialog =
                TimePickerDialog(this,  style, onTimeSetListener, intervalHour, intervalMinute, true)
            timePickerDialog.setTitle("Выберите интервал")
            timePickerDialog.show()
        }

        loadTimeFromInternalStorage()
        loadTimeIntervalFromInternalStorage()

        intervalTextView.text="уведомления через каждые $intervalMinutes минут"
        fromTimeEditText.setText("$fromHour : $fromMinute")
        toTimeEditText.setText("$toHour : $toMinute")
        fromTimeEditText.inputType = InputType.TYPE_NULL
        toTimeEditText.inputType = InputType.TYPE_NULL

        //TODO поправить костыль с долгим отображением таймпикера
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
            //распределение уведомлений
            scheduleNotification(toHour, toMinute)
        }
        else if (fragNumber==1) {
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText2 = findViewById<EditText>(R.id.editTextToTime)
            fromTimeEditText2.setText("$toHour : $toMinute")
            saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
            //распределение уведомлений
            scheduleNotification(toHour, toMinute)
        }
    }

    private fun saveTimeToInternalStorage(prefName1: String, prefName2: String, Hour:Int, Minute:Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(prefName1, Hour)
            putInt(prefName2, Minute)
            apply()
        }
    }

    private fun saveTimeIntervalToInternalStorage(interval:Int){
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
        toHour = sharedPref.getInt(SAVED_TO_HOUR, 23)
        toMinute = sharedPref.getInt(SAVED_TO_MINUTE, 0)
    }

    private fun loadTimeIntervalFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        intervalMinutes = sharedPref.getInt(INTERVAL, 60)
    }

    private fun scheduleNotification(toHour:Int, toMinute:Int){
        val intent = Intent(applicationContext, Notification::class.java)
        val textTitle = "Пришло время пить воду!"
        val textContent = "Нажмите сюда, чтобы открыть приложение."
        intent.putExtra(titleExtra,textTitle)
        intent.putExtra(messageExtra, textContent)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DAY_OF_MONTH, currentDay)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        var iNotificationHour = fromHour
        val iNotificationMinute = toMinute
        val notificationsCount = toHour - fromHour
        //РАСПРЕДЕЛЕНИЕ НАПОМИНАНИЙ
        //TODO Сделать нормальное распределение напоминаний
        for (i in 0..notificationsCount){

            val pendingIntent=PendingIntent.getBroadcast(
                applicationContext,
                i,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (iNotificationHour*60+toMinute >= calendar.get(Calendar.HOUR_OF_DAY)*60+toMinute) {
                calendar.set(Calendar.HOUR_OF_DAY, iNotificationHour)
                calendar.set(Calendar.MINUTE, toMinute)
                val time = calendar.timeInMillis
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                }
                println("_________i: " + i)
                println("iHour: " + iNotificationHour)
                println("iMinute: " + iNotificationMinute)
                println("millis: " + time)
            }
            iNotificationHour++
        }

    }
}