package com.example.app

data class AppData(
    var userInfo: UserInfo? = null,
    var healthData: HealthData? = null,
    var contacts: List<Contact> = emptyList(), // Поле для контактов
    var appUsage: List<AppUsage> = emptyList(), // Поле для использования приложений
    var callLogs: List<CallLogData> = emptyList(),
    var notifications: List<String> = emptyList(),
    var sms: List<String> = emptyList()
)

data class UserInfo(
    val name: String,
    val email: String
)

data class HealthData(
    val steps: Int = 0,
    val stepsHistory: List<Int> = emptyList(),
    val heartRate: Float = 0f,
    val heartRateHistory: List<Float> = emptyList(),
    val calories: Float = 0f,
    val caloriesHistory: List<Float> = emptyList(),
    val distance: Float = 0f,
    val distanceHistory: List<Float> = emptyList(),
    val activeMinutes: Int = 0,
    val sleepDuration: Long = 0L
)


data class Contact(
    val name: String,
    val phoneNumber: String
)

data class AppUsage(
    val appName: String,
    val timeInForeground: Long
)

data class CallLog(
    val name: String,
    val phoneNumber: String
)

