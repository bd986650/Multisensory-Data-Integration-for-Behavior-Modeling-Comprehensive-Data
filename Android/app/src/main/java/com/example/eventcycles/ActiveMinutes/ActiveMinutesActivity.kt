package com.example.eventcycles

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActiveMinutesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_minutes)

        val activeMinutesTextView = findViewById<TextView>(R.id.activeMinutesTextView)

        val prefs = getSharedPreferences("active_minutes_prefs", Context.MODE_PRIVATE)
        val activeMinutes = prefs.getInt("last_active_minutes", 0)

        activeMinutesTextView.text = "Активные минуты за сегодня: $activeMinutes"
    }
}
