package com.example.eventcycles

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HeartRateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate)

        val heartRateTextView = findViewById<TextView>(R.id.heartRateTextView)

        val prefs = getSharedPreferences("heart_prefs", Context.MODE_PRIVATE)
        val bpm = prefs.getInt("last_heart_rate", 0)

        heartRateTextView.text = "Пульс за последнюю минуту: $bpm уд/мин"
    }
}
