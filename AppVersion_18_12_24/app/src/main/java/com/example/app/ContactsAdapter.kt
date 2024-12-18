package com.example.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactsAdapter(
    private val contactsList: List<ContactsData>
) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    // ViewHolder для управления элементами списка
    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactName: TextView = itemView.findViewById(R.id.text_view_contact)
        val contactNumber: TextView = itemView.findViewById(R.id.text_view_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        // Инфлейт макета элемента списка
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contacts, parent, false)
        return ContactsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val contact = contactsList[position]
        holder.contactName.text = contact.contact
        holder.contactNumber.text = contact.number
    }

    override fun getItemCount(): Int = contactsList.size
}
