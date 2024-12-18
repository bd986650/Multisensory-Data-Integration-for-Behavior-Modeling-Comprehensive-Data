package com.example.app

data class HealthSummary(
    val steps: Int,
    val stepHistory: List<Int>,
    val heartRate: Float,
    val heartRateHistory: List<Float>,
    val calories: Float,
    val calorieHistory: List<Float>,
    val distance: Float,
    val distanceHistory: List<Float>,
    //val activeMinutes: Int,
    //val sleepDuration: Long
)
