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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*

class PickTimeForNotifActivity: AppCompatActivity(), TimePickerFragment.OnCompleteListener {

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

    private var notificationsOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pick_time_activity)
        val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTime)
        val toTimeEditText = findViewById<EditText>(R.id.editTextToTime)
        val intervalTextView = findViewById<TextView>(R.id.intervalTextView)
        val intervalChangeButton = findViewById<Button>(R.id.intervalChangeButton)
        val turnOffNotificationsButton = findViewById<ImageButton>(R.id.turnOffNotificationsButton)

        loadNotificationsStateFromInternalStorage()
        loadTimeFromInternalStorage()
        loadIntervalFromInternalStorage()
        saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)

        updateNotifButton()
        updateIntervalTV()

        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, fromHour)
        calendar.set(Calendar.MINUTE, fromMinute)
        fromTimeEditText.setText(df.format(calendar.time))

        calendar.set(Calendar.HOUR_OF_DAY, toHour)
        calendar.set(Calendar.MINUTE, toMinute)
        toTimeEditText.setText(df.format(calendar.time))

        fromTimeEditText.inputType = InputType.TYPE_NULL
        toTimeEditText.inputType = InputType.TYPE_NULL

        //TODO сделать нормальный таймпикер интервала уведомлений, ограниченный по часам и минутам, кратным 5
        fun showIntervalTimePicker(view: View?) {
            val onTimeSetListener =
                TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    scheduleNotifications(0)
                    if ((selectedHour*60 +selectedMinute) <= 180) {
                        intervalHour = selectedHour
                        intervalMinute = selectedMinute
                    }
                    else {
                        Toast.makeText(this, "Выберите не более 3-х часов!", Toast.LENGTH_SHORT).show()
                        intervalHour = 3
                        intervalMinute = 0
                    }
                    updateIntervalTV()
                    saveIntervalToInternalStorage(intervalHour, intervalMinute)
                    //распределение уведомлений
                    scheduleNotifications(1)
                }
            val style = THEME_HOLO_LIGHT
            val timePickerDialog =
                TimePickerDialog(this,  style, onTimeSetListener, intervalHour, intervalMinute, true)
            timePickerDialog.setTitle("Выберите интервал")
            timePickerDialog.show()
        }

        //TODO поправить костыль с долгим отображением таймпикера
        fromTimeEditText.setOnClickListener{
            if (notificationsOn) {
                val bundle = Bundle()
                bundle.putInt("editText", 0)
                val newFragment: DialogFragment = TimePickerFragment()
                newFragment.arguments = bundle
                newFragment.show(supportFragmentManager, "timePicker")
                //showTimePickerDialog(it)
            }
            else Toast.makeText(this, "Сначала включите уведомления!", Toast.LENGTH_SHORT).show()
        }

        toTimeEditText.setOnClickListener{
            if (notificationsOn) {
                val bundle = Bundle()
                bundle.putInt("editText", 1)
                val newFragment: DialogFragment = TimePickerFragment()
                newFragment.arguments = bundle
                newFragment.show(supportFragmentManager, "timePicker")
                //showTimePickerDialog(it)
            }
            else Toast.makeText(this, "Сначала включите уведомления!", Toast.LENGTH_SHORT).show()
        }

        intervalChangeButton.setOnClickListener{
            if (notificationsOn) {
                showIntervalTimePicker(it)
            }
            else Toast.makeText(this, "Сначала включите уведомления!", Toast.LENGTH_SHORT).show()
        }

        turnOffNotificationsButton.setOnClickListener{
            if (notificationsOn) {
                notificationsOn=false
                updateNotifButton()
                Toast.makeText(this, "Уведомления выключены!", Toast.LENGTH_SHORT).show()
                scheduleNotifications(0)
            }
            else {
                notificationsOn=true
                updateNotifButton()
                Toast.makeText(this, "Уведомления включены!", Toast.LENGTH_SHORT).show()
                scheduleNotifications(1)
            }
            updateIntervalTV()
            saveNotificationsStateToInternalStorage()
        }
    }

    //вызывается после установки времени
    override fun onComplete(fragNumber: Int?, calendar: Calendar){
        scheduleNotifications(0)
        val df = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (fragNumber==0) {
            fromHour = calendar.get(Calendar.HOUR_OF_DAY)
            fromMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText = findViewById<EditText>(R.id.editTextFromTime)
            fromTimeEditText.setText(df.format(calendar.time))
            saveTimeToInternalStorage(SAVED_FROM_HOUR, SAVED_FROM_MINUTE, fromHour, fromMinute)
            //распределение уведомлений
            scheduleNotifications(1)
        }
        else if (fragNumber==1) {
            toHour = calendar.get(Calendar.HOUR_OF_DAY)
            toMinute = calendar.get(Calendar.MINUTE)
            val fromTimeEditText2 = findViewById<EditText>(R.id.editTextToTime)
            fromTimeEditText2.setText(df.format(calendar.time))
            saveTimeToInternalStorage(SAVED_TO_HOUR, SAVED_TO_MINUTE, toHour, toMinute)
            //распределение уведомлений
            scheduleNotifications(1)
        }
    }

    private fun updateNotifButton(){
        val turnOffNotificationsButton = findViewById<ImageButton>(R.id.turnOffNotificationsButton)
        if (notificationsOn)
            turnOffNotificationsButton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_outline_notifications_24))
        else
            turnOffNotificationsButton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_baseline_notifications_off_24))
    }

    private fun updateIntervalTV(){
        val intervalTextView = findViewById<TextView>(R.id.intervalTextView)
        if (notificationsOn)
            intervalTextView!!.text = String.format(Locale.getDefault(),
               "уведомления через каждые %02d минут", intervalMinutesCompute())
        else intervalTextView!!.text = "уведомления отключены"
    }

    private fun saveTimeToInternalStorage(prefName1: String, prefName2: String, Hour:Int, Minute:Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(prefName1, Hour)
            putInt(prefName2, Minute)
            apply()
        }
    }

    private fun saveIntervalToInternalStorage(intervalH:Int, intervalM:Int){
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(INTERVALH, intervalH)
            putInt(INTERVALM, intervalM)
            apply()
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

    private fun loadNotificationsStateFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        val oneOrZero = sharedPref.getInt(NOTIFICATIONS, 0)
        if (oneOrZero == 1) notificationsOn = true
            else notificationsOn = false
    }

    private fun loadTimeFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        fromHour = sharedPref.getInt(SAVED_FROM_HOUR, 9)
        fromMinute = sharedPref.getInt(SAVED_FROM_MINUTE, 0)
        toHour = sharedPref.getInt(SAVED_TO_HOUR, 23)
        toMinute = sharedPref.getInt(SAVED_TO_MINUTE, 0)
    }

    private fun loadIntervalFromInternalStorage() {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        intervalHour = sharedPref.getInt(INTERVALH, 1)
        intervalMinute = sharedPref.getInt(INTERVALM, 0)
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

        //TODO попробовать setRepeating()
        //РАСПРЕДЕЛЕНИЕ НАПОМИНАНИЙ
        //TODO Сделать нормальное распределение напоминаний, вызывать его при запуске приложения + в начале суток + после ребута
        var iStart = 0
        if (notificationsOn) {
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
        else {
            //TODO нужен ли FLAG_NO_CREATE? работает?
            if (check == 0) {
                for (i in 0..notificationsCount) {
                    if (timeToSetOn >= calendar.timeInMillis && timeToSetOn <= calendarTo.timeInMillis) {
                        val pendingIntent = PendingIntent.getBroadcast(
                            applicationContext,
                            i,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pendingIntent!=null) {
                            alarmManager.cancel(pendingIntent)
                        }
                        //дебаг
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = timeToSetOn
                        iStart++
                        println("____TURN OFF:_____\nnotif#: " + iStart)
                        println("Hour: " + cal.time)
                        println("millis: " + timeToSetOn)
                    }
                    timeToSetOn += intervalInMillis.toLong()
                }
            }
        }
    }

    private fun intervalMinutesCompute():Int{
        return intervalHour*60+intervalMinute
    }

    private fun intervalMillisCompute():Int{
        return intervalHour*3600000+intervalMinute*60000
    }
}