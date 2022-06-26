package com.example.drinkwaternow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import java.util.*

class FirstLaunchActivity: AppCompatActivity(), TimePickerFragment.OnCompleteListener {
    val APP_PREFERENCES = "Settings"
    private val SAVED_FROM_HOUR = "SavedFromHour"
    private val SAVED_FROM_MINUTE = "SavedFromMinute"
    private val SAVED_TO_HOUR = "SavedToHour"
    private val SAVED_TO_MINUTE = "SavedToMinute"
    private val INTERVALH = "IntervalH"
    private val INTERVALM = "IntervalM"
    private val NOTIFICATIONS = "Notifications"

    private var fromHour = 9
    private var fromMinute = 0
    private var toHour = 23
    private var toMinute = 0

    private var intervalHour = 1
    private var intervalMinute = 0

    private var notificationsOn = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_launch_settings_activity)
        val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTimeFirstLaunch)
        val toTimeEditText = findViewById<EditText>(R.id.editTextToTimeFirstLaunch)
        val continueButton = findViewById<Button>(R.id.continueButton)

        loadTimeFromInternalStorage()
        saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)

        fromTimeEditText.setText("$fromHour : $fromMinute")
        toTimeEditText.setText("$toHour : $toMinute")
        fromTimeEditText.inputType = InputType.TYPE_NULL
        toTimeEditText.inputType = InputType.TYPE_NULL

        fromTimeEditText.setOnClickListener{
                val bundle = Bundle()
                bundle.putInt("editText", 0)
                val newFragment: DialogFragment = TimePickerFragment()
                newFragment.arguments = bundle
                newFragment.show(supportFragmentManager, "timePicker")
        }

        toTimeEditText.setOnClickListener{
                val bundle = Bundle()
                bundle.putInt("editText", 1)
                val newFragment: DialogFragment = TimePickerFragment()
                newFragment.arguments = bundle
                newFragment.show(supportFragmentManager, "timePicker")
        }

        continueButton.setOnClickListener{
            saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
            saveNotificationsStateToInternalStorage()
            finish()
        }
    }

    private fun saveNotificationsStateToInternalStorage(){
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        var oneOrZero = 1
        if (!notificationsOn) oneOrZero = 0
        with (sharedPref.edit()){
            putInt(NOTIFICATIONS, oneOrZero)
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveNotificationsStateToInternalStorage()
        saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
    }

    //вызывается после установки времени
    override fun onComplete(fragNumber: Int?, calendar: Calendar){
        scheduleNotifications(0)
        if (fragNumber==0) {
            fromHour = calendar.get(Calendar.HOUR_OF_DAY)
            fromMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTimeFirstLaunch)
            fromTimeEditText.setText("$fromHour : $fromMinute")
            saveTimeToInternalStorage(SAVED_FROM_HOUR, SAVED_FROM_MINUTE, fromHour, fromMinute)
            //распределение уведомлений
            scheduleNotifications(1)
        }
        else if (fragNumber==1) {
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText2 = findViewById<EditText>(R.id.editTextToTimeFirstLaunch)
            fromTimeEditText2.setText("$toHour : $toMinute")
            saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
            //распределение уведомлений
            scheduleNotifications(1)
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

    private fun loadTimeFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        fromHour = sharedPref.getInt(SAVED_FROM_HOUR, 9)
        fromMinute = sharedPref.getInt(SAVED_FROM_MINUTE, 0)
        toHour = sharedPref.getInt(SAVED_TO_HOUR, 23)
        toMinute = sharedPref.getInt(SAVED_TO_MINUTE, 0)
    }

    private fun scheduleNotifications(check: Int){
        val intent = Intent(applicationContext, Notification::class.java)
        val textTitle = "Пришло время пить воду!"
        val textContent = "Нажмите сюда, чтобы открыть приложение."
        intent.putExtra(titleExtra, textTitle)
        intent.putExtra(messageExtra, textContent)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val calendarTo = Calendar.getInstance()
        val calendarFrom = Calendar.getInstance()
        calendarTo.set(Calendar.HOUR_OF_DAY, toHour)
        calendarTo.set(Calendar.MINUTE, toMinute)
        calendarTo.set(Calendar.SECOND, 0)
        calendarFrom.set(Calendar.HOUR_OF_DAY, fromHour)
        calendarFrom.set(Calendar.MINUTE, fromMinute)
        calendarFrom.set(Calendar.SECOND, 0)

        var timeToSetOn = calendarFrom.timeInMillis
        val intervalInMillis = intervalMillisCompute()
        val fromInMillis = fromHour * 3600000 + fromMinute * 60000
        val toInMillis = toHour * 3600000 + toMinute * 60000
        val notificationsCount = ((toInMillis - fromInMillis) / intervalInMillis)
        //дебаг
        println("_______calendar from time: " + calendarFrom.time)
        println("_______calendar to time: " + calendarTo.time)
        println("_______calendar time: " + calendar.time)
        println("_______calendar interval millis: " + intervalMillisCompute())
        println("____________________NOTIFICATION COUNT:   " + notificationsCount)

        //РАСПРЕДЕЛЕНИЕ НАПОМИНАНИЙ
        var iStart = 0
            for (i in 0..notificationsCount) {
                if (timeToSetOn >= calendar.timeInMillis && timeToSetOn <= calendarTo.timeInMillis) {
                    val pendingIntent = PendingIntent.getBroadcast(
                        applicationContext,
                        i,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            timeToSetOn,
                            pendingIntent
                        )
                    }
                    //дебаг
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = timeToSetOn
                    iStart++
                    println("_________\nnotif#: " + iStart)
                    println("Hour: " + cal.time)
                    println("millis: " + timeToSetOn)
                }
                timeToSetOn += intervalInMillis.toLong()
            }
    }

    private fun intervalMinutesCompute():Int{
        return intervalHour*60+intervalMinute
    }

    private fun intervalMillisCompute():Int{
        return intervalHour*3600000+intervalMinute*60000
    }

}