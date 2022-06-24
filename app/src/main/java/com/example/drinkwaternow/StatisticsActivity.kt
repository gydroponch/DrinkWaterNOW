package com.example.drinkwaternow

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_activity)
        val goBackButton = findViewById<Button>(R.id.goBackButtonStats)
        goBackButton.setOnClickListener{
            finish()
        }
    }
}
