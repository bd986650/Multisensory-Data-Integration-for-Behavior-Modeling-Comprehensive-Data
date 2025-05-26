package com.example.eventcycles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsActivity : AppCompatActivity(), NotificationListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val notificationsArray = NotificationStorage.getNotifications(this)
        val notificationsList = (0 until notificationsArray.length()).map { notificationsArray.getJSONObject(it) }

        val adapter = NotificationAdapter(notificationsList.toMutableList())
        recyclerView.adapter = adapter
    }

    override fun updateRecyclerView() {
        val updatedNotificationsArray = NotificationStorage.getNotifications(this)
        val updatedNotificationsList = (0 until updatedNotificationsArray.length()).map {
            updatedNotificationsArray.getJSONObject(it)
        }

        adapter.updateData(updatedNotificationsList)
    }
}
