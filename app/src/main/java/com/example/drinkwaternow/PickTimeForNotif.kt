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
    private val INTERVALH = "IntervalH"
    private val INTERVALM = "IntervalM"

    private var fromHour = 9
    private var fromMinute = 0
    private var toHour = 23
    private var toMinute = 0

    private var intervalHour = 1
    private var intervalMinute = 0

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
                    intervalTextView!!.text = String.format(Locale.getDefault(), "уведомления через каждые %02d минут", intervalMinutesCompute())
                    saveTimeIntervalToInternalStorage(intervalHour, intervalMinute)
                    //распределение уведомлений
                    scheduleNotification()
                }

            val style = THEME_HOLO_LIGHT
            val timePickerDialog =
                TimePickerDialog(this,  style, onTimeSetListener, intervalHour, intervalMinute, true)
            timePickerDialog.setTitle("Выберите интервал")
            timePickerDialog.show()
        }

        loadTimeFromInternalStorage()
        loadTimeIntervalFromInternalStorage()
        saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
        //распределение уведомлений
        scheduleNotification()

        intervalTextView.text="уведомления через каждые ${intervalMinutesCompute()} минут"
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
            scheduleNotification()
        }
        else if (fragNumber==1) {
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText2 = findViewById<EditText>(R.id.editTextToTime)
            fromTimeEditText2.setText("$toHour : $toMinute")
            saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
            //распределение уведомлений
            scheduleNotification()
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

    private fun saveTimeIntervalToInternalStorage(intervalH:Int, intervalM:Int){
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(INTERVALH, intervalH)
            putInt(INTERVALM, intervalM)
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
        intervalHour = sharedPref.getInt(INTERVALH, 1)
        intervalMinute = sharedPref.getInt(INTERVALM, 0)
    }

    private fun scheduleNotification(){
        val intent = Intent(applicationContext, Notification::class.java)
        val textTitle = "Пришло время пить воду!"
        val textContent = "Нажмите сюда, чтобы открыть приложение."
        intent.putExtra(titleExtra,textTitle)
        intent.putExtra(messageExtra, textContent)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val calendarTo = Calendar.getInstance()
        val calendarFrom = Calendar.getInstance()
        calendarTo.set(Calendar.HOUR_OF_DAY,toHour)
        calendarTo.set(Calendar.MINUTE,toMinute)
        calendarFrom.set(Calendar.HOUR_OF_DAY,fromHour)
        calendarFrom.set(Calendar.MINUTE,fromMinute)


        var timeToSetOn = calendar.timeInMillis
        val intervalInMillis = intervalMillisCompute()
        val fromInMillis = fromHour * 3600000 + fromMinute*60000
        val toInMillis = toHour * 3600000 + toMinute*60000
        val notificationsCount = ((toInMillis-fromInMillis)/intervalInMillis)
        println("_______calendar from time: " + calendarFrom.time)
        println("_______calendar to time: " + calendarTo.time)
        println("_______calendar time: " + calendar.time)
        println("_______calendar interval millis: " + intervalMillisCompute())
        println("____________________NOTIFICATION COUNT:   " + notificationsCount)

        //РАСПРЕДЕЛЕНИЕ НАПОМИНАНИЙ
        //TODO Сделать нормальное распределение напоминаний, вызывать его при запуске приложения + в начале суток + после ребута
        var iCurrentTime = calendarFrom.timeInMillis
        var iStart = 0
        for (i in 0..notificationsCount){
            if (iCurrentTime >= calendar.timeInMillis) {
                val pendingIntent=PendingIntent.getBroadcast(
                    applicationContext,
                    i,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                timeToSetOn += intervalInMillis.toLong()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeToSetOn,
                        pendingIntent
                    )
                }
                //дебаг
                val cal = Calendar.getInstance()
                cal.timeInMillis=timeToSetOn
                iStart++
                println("_________\nnotif#: " + iStart)
                println("Hour: " + cal.time)
                println("millis: " + timeToSetOn)
            }
            iCurrentTime += intervalInMillis.toLong()
        }
    }

    private fun intervalMinutesCompute():Int{
        return intervalHour*60+intervalMinute
    }

    private fun intervalMillisCompute():Int{
        return intervalHour*3600000+intervalMinute*60000
    }
}