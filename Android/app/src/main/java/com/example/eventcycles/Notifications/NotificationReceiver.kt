package com.example.eventcycles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = intent.getStringExtra("app") ?: "Неизвестное приложение"
        val title = intent.getStringExtra("title") ?: "Без заголовка"
        val message = intent.getStringExtra("message") ?: "Нет текста"

        NotificationStorage.saveNotification(context, app, title, message)
    }
}
