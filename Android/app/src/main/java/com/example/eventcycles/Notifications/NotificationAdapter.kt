package com.example.eventcycles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(private val notifications: MutableList<JSONObject>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val title: TextView = view.findViewById(R.id.notificationTitle)
        val message: TextView = view.findViewById(R.id.notificationMessage)
        val timestamp: TextView = view.findViewById(R.id.notificationTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position] // Получаем объект из списка

        holder.appName.text = notification.optString("app", "Неизвестное приложение")
        holder.title.text = notification.optString("title", "Без заголовка")
        holder.message.text = notification.optString("message", "Нет текста")
        val timestampMillis = notification.optLong("timestamp", 0L)

        // Форматируем дату
        val date = Date(timestampMillis)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val formattedDate = formatter.format(date)

        holder.timestamp.text = formattedDate
    }

    override fun getItemCount(): Int = notifications.size

    // Функция для обновления списка
    fun updateData(newNotifications: List<JSONObject>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()  // Обновляем список
    }
}
