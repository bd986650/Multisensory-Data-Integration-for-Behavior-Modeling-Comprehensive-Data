package com.example.app

// CallLogData.kt

data class CallLogData(
    val number: String,  // Номер телефона
    val date: String,    // Дата звонка (как строка)
    val type: String     // Тип звонка (например, входящий, исходящий, пропущенный)
)
