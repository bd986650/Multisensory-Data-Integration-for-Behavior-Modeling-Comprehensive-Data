package com.example.eventcycles

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CaloriesActivity : AppCompatActivity() {

    private lateinit var caloriesTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calories)

        caloriesTextView = findViewById(R.id.caloriesTextView)

        val prefs = getSharedPreferences("calories_prefs", MODE_PRIVATE)
        val calories = prefs.getInt("last_calories", 0)

        caloriesTextView.text = "Сожжённые калории за сегодня: $calories ккал"
    }
}
