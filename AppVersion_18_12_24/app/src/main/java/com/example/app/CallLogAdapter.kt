package com.example.app

// CallLogAdapter.kt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CallLogAdapter(private val callLogs: List<CallLogData>) : RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_call_log, parent, false)
        return CallLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallLogViewHolder, position: Int) {
        val callLog = callLogs[position]
        holder.bind(callLog)
    }

    override fun getItemCount(): Int = callLogs.size

    inner class CallLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberTextView: TextView = itemView.findViewById(R.id.text_view_number)
        private val dateTextView: TextView = itemView.findViewById(R.id.text_view_date)
        private val typeTextView: TextView = itemView.findViewById(R.id.text_view_type)

        fun bind(callLog: CallLogData) {
            numberTextView.text = callLog.number
            dateTextView.text = callLog.date
            typeTextView.text = callLog.type
        }
    }
}
