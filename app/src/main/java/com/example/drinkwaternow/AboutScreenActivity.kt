package com.example.drinkwaternow

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.drinkwaternow.BuildConfig

class AboutScreenActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_screen_activity)
        val goBackButton = findViewById<Button>(R.id.goBackButton)
        val aboutTextView = findViewById<TextView>(R.id.aboutTextView)

        val version = BuildConfig.VERSION_NAME
        val aboutText = resources.getString(R.string.AboutText)
        val aboutString = "$aboutText\nВерсия: $version"
        aboutTextView.text = aboutString

        goBackButton.setOnClickListener{
            finish()
        }
    }
}