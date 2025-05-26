package com.example.eventcycles

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StepsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_steps)

        val stepsTextView = findViewById<TextView>(R.id.stepsTextView)

        val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        val steps = prefs.getInt("last_steps", 0)

        stepsTextView.text = "Шаги за сегодня: $steps"
    }
}
