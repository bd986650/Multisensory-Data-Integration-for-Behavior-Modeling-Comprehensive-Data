package com.example.app

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class HealthActivity : AppCompatActivity() {

    private lateinit var stepsTextView: TextView
    private lateinit var stepHistoryTextView: TextView
    private lateinit var heartRateTextView: TextView
    private lateinit var heartRateHistoryTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var activeMinutesTextView: TextView
    private lateinit var sleepDurationTextView: TextView



    private lateinit var dataManager: DataManager

    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Обновляем данные сердцебиения
            dataManager.getHeartRateData { newHeartRate ->
                heartRateTextView.text = "Сердцебиение: $newHeartRate"
                dataManager.updateHeartRateHistory(newHeartRate)
                val heartRateHistory = dataManager.loadHeartRateHistory()
                heartRateHistoryTextView.text = "История сердцебиения: ${heartRateHistory.joinToString(", ")}"
            }

            // Обновляем данные шагов
            dataManager.getStepData { newSteps ->
                stepsTextView.text = "Шаги: $newSteps"
                dataManager.updateStepHistory(newSteps)
                val stepHistory = dataManager.loadStepHistory()
                stepHistoryTextView.text = "История шагов: ${stepHistory.joinToString(", ")}"
            }

            dataManager.getCalorieData { newCalories ->
                caloriesTextView.text = "Сожженные калории: $newCalories"
                dataManager.updateCalorieHistory(newCalories)
                val calorieHistory = dataManager.loadCalorieHistory()
                val calorieHistoryString = calorieHistory.joinToString(", ")
                findViewById<TextView>(R.id.text_calorie_history).text = "История калорий: $calorieHistoryString"
            }

            dataManager.getDistanceData { newDistance ->
                distanceTextView.text = "Пройденное расстояние: $newDistance м"
                dataManager.updateDistanceHistory(newDistance)
                val distanceHistory = dataManager.loadDistanceHistory()
                findViewById<TextView>(R.id.text_distance_history).text =
                    "История дистанции: ${distanceHistory.joinToString(", ")}"
            }


            // Повторяем обновление через 60 секунд
            handler.postDelayed(this, 60 * 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health)

        // Инициализация TextView для отображения данных
        stepsTextView = findViewById(R.id.text_steps)
        stepHistoryTextView = findViewById(R.id.text_step_history)
        heartRateTextView = findViewById(R.id.text_heart_rate)
        heartRateHistoryTextView = findViewById(R.id.text_heart_rate_history)
        caloriesTextView = findViewById(R.id.text_calories)
        distanceTextView = findViewById(R.id.text_distance)
        activeMinutesTextView = findViewById(R.id.text_active_minutes)
        sleepDurationTextView = findViewById(R.id.text_sleep_duration)

        dataManager = DataManager(this)

        // Получаем данные из Intent
        val steps = intent.getIntExtra("EXTRA_STEPS", 0)
        val heartRate = intent.getFloatExtra("EXTRA_HEART_RATE", 0f)
        val calories = intent.getFloatExtra("EXTRA_CALORIES", 0f)
        val distance = intent.getFloatExtra("EXTRA_DISTANCE", 0f)
        val activeMinutes = intent.getIntExtra("EXTRA_ACTIVE_MINUTES", 0)
        val weight = intent.getFloatExtra("EXTRA_WEIGHT", 0f)
        val height = intent.getFloatExtra("EXTRA_HEIGHT", 0f)
        val bmi = intent.getFloatExtra("EXTRA_BMI", 0f)
        val sleepDuration = intent.getLongExtra("EXTRA_SLEEP_DURATION", 0L)

        val hours = TimeUnit.MILLISECONDS.toHours(sleepDuration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(sleepDuration) % 60

        // Устанавливаем данные в TextView
        stepsTextView.text = "Шаги: $steps"
        heartRateTextView.text = "Сердцебиение: $heartRate"
        caloriesTextView.text = "Сожженные калории: $calories"
        distanceTextView.text = "Пройденное расстояние: $distance м"
        activeMinutesTextView.text = "Активные минуты: $activeMinutes"
        sleepDurationTextView.text = "Продолжительность сна: $hours ч $minutes мин"

        // Отображаем историю шагов и сердцебиений
        val heartRateHistory = dataManager.loadHeartRateHistory()
        heartRateHistoryTextView.text = "История сердцебиения: ${heartRateHistory.joinToString(", ")}"
        val stepHistory = dataManager.loadStepHistory()
        stepHistoryTextView.text = "История шагов: ${stepHistory.joinToString(", ")}"
        val distanceHistory = dataManager.loadDistanceHistory()
        findViewById<TextView>(R.id.text_distance_history).text =
            "История дистанции: ${distanceHistory.joinToString(", ")}"

        // Инициализация UI
        dataManager.generateHealthSummary { summary ->
            updateUI(summary)
        }

        // Запускаем автоматическое обновление
        handler.post(updateRunnable)
    }

    private fun updateUI(summary: HealthSummary) {
        stepsTextView.text = "Шаги: ${summary.steps}"
        stepHistoryTextView.text = "История шагов: ${summary.stepHistory.joinToString(", ")}"
        heartRateTextView.text = "Сердцебиение: ${summary.heartRate}"
        heartRateHistoryTextView.text = "История сердцебиения: ${summary.heartRateHistory.joinToString(", ")}"
        caloriesTextView.text = "Сожженные калории: ${summary.calories}"
        distanceTextView.text = "Пройденное расстояние: ${summary.distance} м"

//        activeMinutesTextView.text = "Активные минуты: ${summary.activeMinutes}"
//        sleepDurationTextView.text = "Продолжительность сна: ${
//            TimeUnit.MILLISECONDS.toHours(summary.sleepDuration)
//        } ч ${
//            TimeUnit.MILLISECONDS.toMinutes(summary.sleepDuration) % 60
//        } мин"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)  // Останавливаем обновления
    }
}
