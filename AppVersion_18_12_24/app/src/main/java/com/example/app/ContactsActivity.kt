package com.example.app


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContactsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_contacts)

        // Получаем список контактов
        val contacts = getContacts(this)

        // Настраиваем RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ContactsAdapter(contacts)
    }

    @SuppressLint("Range")
    fun getContacts(context: Context): List<ContactsData> {
        val contactsList = mutableListOf<ContactsData>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Неизвестный"
                val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: "Неизвестный"
                contactsList.add(ContactsData(name, number))
            }
        }
        return contactsList
    }
}
