package com.example.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var noMessagesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerView = findViewById(R.id.recycler_view_messages)
        noMessagesTextView = findViewById(R.id.no_messages_text)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Получаем сохраненные SMS
        val smsList = MyNotificationListener.getSavedSMS(this)

        if (smsList.isEmpty()) {
            Log.d("MessagesActivity", "No saved messages found.")
            noMessagesTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            Log.d("MessagesActivity", "Loaded messages: ${smsList.size}")
            noMessagesTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            messageAdapter = MessageAdapter(smsList)
            recyclerView.adapter = messageAdapter
        }
    }
}
