package com.example.eventcycles

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName // Приложение, отправившее уведомление
        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString() ?: "Без заголовка"
        val message = extras.getCharSequence("android.text")?.toString() ?: "Нет текста"

        Log.d("NotificationService", "New notification from $packageName: $title - $message")
        // MetricSender.sendNotification(this, packageName, title, message, System.currentTimeMillis() / 1000)

        NotificationStorage.saveNotification(this, packageName, title, message)
    }
}
