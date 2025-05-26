package com.example.eventcycles

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object NotificationStorage {
    private const val FILE_NAME = "notifications.json"
    private const val MAX_NOTIFICATIONS = 20 // Увеличили до 20 уведомлений
    private const val TAG = "NotificationStorage"

    fun saveNotification(context: Context, app: String, title: String?, text: String) {
        val file = File(context.filesDir, FILE_NAME)
        val notificationsArray: JSONArray = if (file.exists()) {
            JSONArray(file.readText()) // Читаем существующий массив
        } else {
            JSONArray()
        }

        // val formattedTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val notificationJson = JSONObject().apply {
            put("app", app)
            put("title", title ?: "Без заголовка")
            put("message", text)
            put("timestamp", System.currentTimeMillis())
        }

        // Увеличиваем массив, копируя элементы на один вправо
        if (notificationsArray.length() >= MAX_NOTIFICATIONS) {
            for (i in notificationsArray.length() - 1 downTo 1) {
                notificationsArray.put(i, notificationsArray.getJSONObject(i - 1))
            }
        } else {
            for (i in notificationsArray.length() - 1 downTo 1) {
                notificationsArray.put(i, notificationsArray.getJSONObject(i - 1))
            }
            notificationsArray.put(0, JSONObject()) // Временное место
        }

        // Вставляем новое уведомление в начало
        notificationsArray.put(0, notificationJson)

        // Ограничиваем количество сохранённых уведомлений до 20
        while (notificationsArray.length() > MAX_NOTIFICATIONS) {
            notificationsArray.remove(notificationsArray.length() - 1)
        }

        // Сохраняем обновлённый массив в файл
        file.writeText(notificationsArray.toString())

        // Создаём JSON только с последним уведомлением
        val lastNotificationJson = notificationJson.toString()

        // Создаём **отдельный JSON-файл** только для последнего уведомления
        val lastNotificationFile = File(context.filesDir, "last_notification.json")
        lastNotificationFile.writeText(notificationJson.toString())

        Log.d(TAG, "Added notification: $notificationJson")
        Log.d(TAG, "All notifications: $notificationsArray")
    }

    fun getNotifications(context: Context): JSONArray {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) JSONArray(file.readText()) else JSONArray()
    }
}
