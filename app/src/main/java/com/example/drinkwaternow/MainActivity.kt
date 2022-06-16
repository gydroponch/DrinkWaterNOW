package com.example.drinkwaternow

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), ChangeCupDialogFragment.StringListener, EditGoalDialog.StringListener {
    @SuppressLint("SetTextI18n")

    private val APP_PREFERENCES = "Settings"
    private var todayGoal = 2500
    private var drankToday = 0
    private var progressAmount = 250
    private var isAnimationFinished = true
    private var cupsList = listOf(200, 250, 500, 1000  /*, 0*/)
    private var todayGoalDone = false
    val CHANNEL_ID = "DEF_CHANNEL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        createNotificationChannel(CHANNEL_ID)

        val addWaterButton = findViewById<Button>(R.id.addWaterButton)
        val pickTimeForNotifActivityButton = findViewById<ImageButton>(R.id.pickTimeForNotifActivityButton)
        val clearWaterButton = findViewById<ImageButton>(R.id.zeroWaterButton)
        val changeCupButton = findViewById<ImageButton>(R.id.changeCupButton)
        val editGoalButton = findViewById<ImageButton>(R.id.editGoalButton)

        drankToday = loadWaterCountToInternalStorage()
        progressAmount = loadChosenCup()
        todayGoal = loadTodayGoal()
        updateCountTextDisplay()
        updateProgressBar(drankToday)

        addWaterButton.setOnClickListener{
            if (isAnimationFinished) {
                drankToday += progressAmount
                updateCountTextDisplay()
                updateProgressBar(progressAmount)
            }
            saveWaterCountToInternalStorage(drankToday)
        }

        clearWaterButton.setOnClickListener{
            if (isAnimationFinished) {
                if (drankToday != 0) {
                    drankToday = 0
                    updateCountTextDisplay()
                    clearProgressBar()
                    todayGoalDone = false
                    saveWaterCountToInternalStorage(drankToday)
                }
            }
        }

        editGoalButton.setOnClickListener(){
            showEditGoalDialog()
        }

        changeCupButton.setOnClickListener{
            showChangeCupDialogFragment()
        }

        pickTimeForNotifActivityButton.setOnClickListener{
            val notifIntent = Intent(applicationContext, PickTimeForNotif::class.java)
            startActivity(notifIntent)
        }
    }

    //проверка на выполнение плана
    private fun isTodayGoalDone(): Boolean {
        return drankToday >= todayGoal
    }

    private fun updateCountTextDisplay (){
        val drankWaterTextView = findViewById<TextView>(R.id.drankWaterTextView)
        val displayedText = "$drankToday из $todayGoal мл"
        drankWaterTextView.text = displayedText
        //увеличение прогресса прогрессбара без анимации
        //todayWaterProgressBar.progress = (clickCounter.toDouble()/TODAY_GOAL.toDouble()*100).toInt()
    }

    private fun showTodayGoalDoneToast(){
        if (isTodayGoalDone()) {
            //displayTodayGoalDone()
            if (!todayGoalDone){
                Toast.makeText(applicationContext, getString(R.string.todayGoalDoneString), Toast.LENGTH_SHORT).show()
                todayGoalDone = true
            }
        }
    }

    fun updateProgressBar(progress: Int){
        val todayWaterProgressBar = findViewById<ProgressBar>(R.id.todayWaterProgressBar)
        val increaseAmount = (((progress.toDouble()/todayGoal.toDouble())*100)*2).toInt()
        val progressBarIncreaseTo = todayWaterProgressBar.progress + increaseAmount*100
        //TODO убрать строчки для дебага
        //для дебага
        println("___\nwater progress: $progress")
        println("GOAL: $todayGoal")
        println("animation increase amount: $increaseAmount")
        println("animation increase to: $progressBarIncreaseTo")
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

    override fun chosenCupSendInput(input: String) {
        //выбранная чашка
        progressAmount=input.toInt()
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt(getString(R.string.chosenCup), progressAmount)
        editor.apply()
        Toast.makeText(applicationContext,"Выбрана чашка ($input) мл",Toast.LENGTH_SHORT).show()
        println(input)
    }

    override fun editGoalSendInput(input: String) {
        //введённая цель
        //TODO окружить toInt() try-catch и обрабатывать NumberFormatException
        todayGoal = input.toInt()
        updateTodayGoal()
        recalculateProgressBar()
        saveTodayGoalToInternalStorage(todayGoal)
    }

    private fun clearProgressBar(){
        val progressBar = findViewById<ProgressBar>(R.id.todayWaterProgressBar)
        progressBar.progress=0
    }

    private fun recalculateProgressBar(){
        val progressBar = findViewById<ProgressBar>(R.id.todayWaterProgressBar)
        progressBar.progress=(((drankToday.toDouble()/todayGoal.toDouble())*100)*2).toInt()*100
    }

    private fun showChangeCupDialogFragment(){
        val dialogFragment = ChangeCupDialogFragment(cupsList)
        dialogFragment.show(supportFragmentManager, "Change cup!")
    }

    private fun showEditGoalDialog(){
        val dialogFragment = EditGoalDialog()
        dialogFragment.show(supportFragmentManager, "Edit goal!")
    }

    private fun saveWaterCountToInternalStorage(clickCounter: Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(getString(R.string.clickCount), clickCounter)
            apply()
        }
    }

    private fun saveTodayGoalToInternalStorage(todayGoal: Int) {
        val sharedPref = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        with (sharedPref.edit()){
            putInt(getString(R.string.todayGoal), todayGoal)
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

    private fun updateTodayGoal(){
        val displayedText = "$drankToday из $todayGoal мл"
        val drankWaterTextView = findViewById<TextView>(R.id.drankWaterTextView)
        drankWaterTextView.text = displayedText
    }

    private fun createNotificationChannel(CHANNEL_ID: String){
        //канал для уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            //notificationManager.deleteNotificationChannel("DEF_CHANNEL")
            val channelName = "Напоминания"
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()
            val sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/raw/notification_water_sound")
            channel.description = "Уведомления с напоминанием выпить воду"
            channel.enableLights(true)
            channel.setSound(sound, attr)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
