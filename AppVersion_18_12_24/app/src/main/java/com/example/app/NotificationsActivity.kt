// NotificationsActivity.kt
package com.example.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val notificationsTextView = findViewById<TextView>(R.id.notificationsTextView)

        // Получаем сохранённые уведомления и выводим их
        val savedNotifications = MyNotificationListener.getSavedNotifications(this)
        if (savedNotifications.isEmpty()) {
            notificationsTextView.text = "No notifications yet"
        } else {
            notificationsTextView.text = savedNotifications.joinToString("\n")
        }
    }
}
