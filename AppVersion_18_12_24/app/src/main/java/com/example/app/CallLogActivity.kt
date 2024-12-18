package com.example.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CallLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CallLogAdapter
    val callLogs = mutableListOf<CallLogData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_log)

        // Инициализация RecyclerView и адаптера
        recyclerView = findViewById(R.id.recycler_view_call_logs)
        adapter = CallLogAdapter(callLogs)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Проверка разрешений на доступ к журналу звонков
        checkCallLogPermission()
    }

    // Метод для проверки разрешений
    fun checkCallLogPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // Запрос разрешения на чтение журнала звонков
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), REQUEST_CODE_CALL_LOG_PERMISSION)
        } else {
            loadCallLogs()  // Если разрешение уже есть, загружаем звонки
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CALL_LOG_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadCallLogs()  // Загружаем звонки, если разрешение предоставлено
        } else {
            Toast.makeText(this, "Разрешение на доступ к журналу звонков не предоставлено.", Toast.LENGTH_SHORT).show()
        }
    }

    // Загружаем данные звонков
    @SuppressLint("Range")
    fun loadCallLogs() {
        // Получение данных журнала звонков
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val number = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                val date = it.getString(it.getColumnIndex(CallLog.Calls.DATE))
                val type = it.getString(it.getColumnIndex(CallLog.Calls.TYPE))

                // Добавление данных звонка в список callLogs
                callLogs.add(CallLogData(number, date, type))
            }
            adapter.notifyDataSetChanged() // Обновляем RecyclerView после добавления данных
        }
    }

    companion object {
        private const val REQUEST_CODE_CALL_LOG_PERMISSION = 1
    }
}
