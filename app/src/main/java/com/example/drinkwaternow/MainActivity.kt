package com.example.drinkwaternow

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : AppCompatActivity(), ChangeCupDialogFragment.StringListener {
    @SuppressLint("SetTextI18n")

    private val APP_PREFERENCES = "Settings"
    private var todayGoal = 2500
    private var drankToday = 0
    private var progressAmount = 250
    private var isAnimationFinished = true
    private var cupsList = listOf(200, 250, 500, 1000  /*, 0*/)
    private var todayGoalDone = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        //уведомления
        val CHANNEL_ID = "DEF_CHANNEL"
        createNotificationChannel(CHANNEL_ID)
        //val notificationBuilder = getNotificationBuilder(CHANNEL_ID)

        val addWaterButton = findViewById<Button>(R.id.addWaterButton)
        val pickTimeForNotifActivityButton = findViewById<ImageButton>(R.id.pickTimeForNotifActivityButton)
        val clearWaterButton = findViewById<ImageButton>(R.id.zeroWaterButton)
        val changeCupButton = findViewById<ImageButton>(R.id.changeCupButton)
        val drankWaterTextView = findViewById<TextView>(R.id.drankWaterTextView)
        val todayWaterProgressBar = findViewById<ProgressBar>(R.id.todayWaterProgressBar)

        //проверка на выполнение плана
        fun isTodayGoalDone(): Boolean {
            return drankToday >= todayGoal
        }

        fun showTodayGoalDoneToast(){
            if (isTodayGoalDone()) {
                //displayTodayGoalDone()
                if (!todayGoalDone){
                    Toast.makeText(applicationContext, getString(R.string.todayGoalDoneString), Toast.LENGTH_SHORT).show()
                    todayGoalDone = true
                }
            }
        }

        //увеличение прогресса прогрессбара с анимацией
        fun updateProgressBar(progress: Int){
            val increaseAmount = (((progress.toDouble()/todayGoal.toDouble())*100)*2).toInt()
            val progressBarIncreaseTo = todayWaterProgressBar.progress + increaseAmount*100

            //TODO убрать строчки для дебага
            //для дебага
            println("___\nwater progress: $progress")
            println("GOAL: $todayGoal")
            println("animation increase amount: $increaseAmount")
            println("animation increase to: $progressBarIncreaseTo")

            if (progress == 0) {
                todayWaterProgressBar.progress = 0
            }
            else {
                val animation = ObjectAnimator.ofInt(todayWaterProgressBar, "progress", todayWaterProgressBar.progress, progressBarIncreaseTo)
                animation.duration = 200
                animation.interpolator = DecelerateInterpolator()
                animation.setAutoCancel(true)
                isAnimationFinished = false
                animation.addListener( object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        isAnimationFinished = true
                        showTodayGoalDoneToast()
                    }
                } )
                animation.start()
            }
        }

        //обновление счётчика воды
        fun updateCountTextDisplay (progress: Int){
            val displayedText = "$drankToday из $todayGoal мл"
            drankWaterTextView.text = displayedText
            updateProgressBar(progress)
            //увеличение прогресса прогрессбара без анимации
            //todayWaterProgressBar.progress = (clickCounter.toDouble()/TODAY_GOAL.toDouble()*100).toInt()

        }

        drankToday = loadWaterCountToInternalStorage()
        progressAmount = loadChosenCup()
        updateCountTextDisplay(drankToday)

        addWaterButton.setOnClickListener{
            if (isAnimationFinished) {
                drankToday += progressAmount
                updateCountTextDisplay(progressAmount)
            }

            saveWaterCountToInternalStorage(drankToday)
        }

        clearWaterButton.setOnClickListener{
            if (isAnimationFinished) {
                if (drankToday != 0) {
                    drankToday = 0
                    updateCountTextDisplay(0)
                    todayWaterProgressBar.progress = 0
                    todayGoalDone = false
                    saveWaterCountToInternalStorage(drankToday)
                }
            }
        }

        changeCupButton.setOnClickListener{
            showChangeCupDialogFragment()
        }

        pickTimeForNotifActivityButton.setOnClickListener{
            val notifIntent = Intent(applicationContext, PickTimeForNotif::class.java)
            startActivity(notifIntent)
        }
    }



    override fun sendInput(input: String) {
        //выбранная чашка
        progressAmount=input.toInt()
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(getString(R.string.chosenCup), progressAmount)
        editor.apply()
        Toast.makeText(applicationContext,"Выбрана чашка ($input) мл",Toast.LENGTH_SHORT).show()
        println(input)
    }

    private fun showChangeCupDialogFragment(){
        val dialogFragment = ChangeCupDialogFragment(cupsList)
        dialogFragment.show(supportFragmentManager, "Change cup!")
    }

    private fun saveWaterCountToInternalStorage(clickCounter: Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(getString(R.string.clickCount), clickCounter)
            apply()
        }
    }

    private fun saveTodayGoalToInternalStorage(clickCounter: Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(getString(R.string.todayGoal), clickCounter)
            apply()
        }
    }

    private fun loadWaterCountToInternalStorage(): Int {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPref.getInt(getString(R.string.clickCount), 0)
    }

    private fun loadChosenCup(): Int {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPref.getInt(getString(R.string.chosenCup), 250)
    }

    private fun loadTodayGoal(): Int {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPref.getInt(getString(R.string.todayGoal), 2500)
    }

    private fun createNotificationChannel(CHANNEL_ID: String){
        //канал для уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID, "Напоминания",
                NotificationManager.IMPORTANCE_HIGH
            )
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()
            val sound = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.notification_water_2)
            channel.description = "Включает в себя уведомления с напоминанием выпить воду"
            channel.enableLights(true)
            channel.lightColor = Color.WHITE
            channel.enableVibration(false)
            channel.setSound(sound, attr);
            notificationManager.createNotificationChannel(channel)
        }
    }
    /*
    private fun getNotificationBuilder(CHANNEL_ID: String): NotificationCompat.Builder {
        //текст и содержимое уведомления
        val textTitle = "Пришло время пить воду!"
        val textContent = "Нажмите сюда, чтобы открыть приложение."

        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        //задание параметров уведомления
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_outline_local_drink_24)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }
     */
}
