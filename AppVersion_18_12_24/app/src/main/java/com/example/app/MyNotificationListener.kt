package com.example.app

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import android.content.ContentResolver
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.app.Notification

class MyNotificationListener : NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListener", "NotificationListenerService initialized")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val packageName = it.packageName
            val extras = it.notification.extras

            Log.d("NotificationListener", "Received notification from $packageName")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.d("NotificationListener", "Extras key: $key, value: $value")
            }

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No Text"
            val timestamp = System.currentTimeMillis()

            Log.d("NotificationListener", "Final title: $title, text: $text")

            // Save notification to SharedPreferences
            saveNotificationToSharedPreferences(packageName, title, text, timestamp)

            // Save SMS in SharedPreferences
            saveSMSInSharedPreferences()
        }
    }

    private fun saveNotificationToSharedPreferences(packageName: String, title: String?, text: String?, timestamp: Long) {
        Log.d("NotificationListener", "Saving notification to SharedPreferences")

        val sharedPrefs = getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val notificationsJson = sharedPrefs.getString("notifications", "[]")
        val notificationsArray = JSONArray(notificationsJson)

        val notificationObject = JSONObject().apply {
            put("package", packageName)
            put("title", title)
            put("text", text)
            put("timestamp", timestamp)
        }

        notificationsArray.put(notificationObject)
        editor.putString("notifications", notificationsArray.toString())
        editor.apply()

        Log.d("NotificationListener", "Saved notification: $packageName - $title - $text")
    }

    private fun saveSMSInSharedPreferences() {
        val contentResolver: ContentResolver = contentResolver
        val cursor = contentResolver.query(
            Uri.parse("content://sms/inbox"),
            null,
            null,
            null,
            null
        )

        if (cursor != null && cursor.count > 0) {
            Log.d("NotificationListener", "Found ${cursor.count} SMS messages.")
            val sharedPrefs = getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()

            val smsJson = sharedPrefs.getString("sms", "[]")
            val smsArray = JSONArray(smsJson)

            while (cursor.moveToNext()) {
                val address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                val body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                val date = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(date))

                val smsObject = JSONObject().apply {
                    put("from", address)
                    put("message", body)
                    put("date", formattedDate)
                }

                smsArray.put(smsObject)
            }

            editor.putString("sms", smsArray.toString())
            editor.apply()

            Log.d("NotificationListener", "Saved SMS: ${smsArray.toString()}")
        } else {
            Log.d("NotificationListener", "No SMS found or permission denied.")
        }
        cursor?.close()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("NotificationListener", "Notification was removed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NotificationListener", "NotificationListenerService stopped")
    }

    companion object {
        fun getSavedNotifications(context: Context): List<String> {
            Log.d("NotificationListener", "Getting saved notifications from SharedPreferences")
            val sharedPrefs = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
            val notificationsJson = sharedPrefs.getString("notifications", "[]")
            val notificationsArray = JSONArray(notificationsJson)
            val notificationsList = mutableListOf<String>()

            for (i in 0 until notificationsArray.length()) {
                val notification = notificationsArray.getJSONObject(i)
                val packageName = notification.getString("package")
                val title = notification.optString("title")
                val text = notification.optString("text")
                val timestamp = notification.getLong("timestamp")

                notificationsList.add("[$packageName] $title: $text at $timestamp")
            }

            Log.d("NotificationListener", "Saved notifications retrieved: $notificationsList")
            return notificationsList
        }

        fun getSavedSMS(context: Context): List<String> {
            Log.d("NotificationListener", "Getting saved SMS from SharedPreferences")
            val sharedPrefs = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
            val smsJson = sharedPrefs.getString("sms", "[]")
            val smsArray = JSONArray(smsJson)
            val smsList = mutableListOf<String>()

            for (i in 0 until smsArray.length()) {
                val sms = smsArray.getJSONObject(i)
                val from = sms.getString("from")
                val message = sms.getString("message")
                val date = sms.getString("date")

                smsList.add("From: $from\nMessage: $message\nDate: $date")
            }

            Log.d("NotificationListener", "Saved SMS retrieved: $smsList")
            return smsList
        }
    }
}
