@file:Suppress("DEPRECATION")

package com.example.app

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    companion object {
        const val SMS_PERMISSION_CODE = 100
        const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101
        const val REQUEST_ACTIVITY_RECOGNITION_CODE = 102
    }

    private lateinit var stepCounter: StepCounter
    private lateinit var heartRateMonitor: HeartRateMonitor
    private lateinit var calorieTracker: CalorieTracker
    private lateinit var distanceTracker: DistanceTracker
    private lateinit var activeMinutesTracker: ActiveMinutesTracker
    private lateinit var sleepTracker: SleepTracker

//    private lateinit var stepsHistory: DataManager
//    private lateinit var heartRateHistory: DataManager
//    private lateinit var calorieHistory: DataManager
//    private lateinit var distanceHistory: DataManager

    private lateinit var screenTimeTextView: TextView
    private val updateHandler = Handler()

    private val appData = AppData()
    private val authActivity = AuthManager(this)
    private var pendingOperations = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 5 * 60 * 1000 // 5 минут в миллисекундах
    private var isInitialDataSent = false // Флаг для первичной отправки
    private var isInitialDelayStarted = false // Новый флаг для задержки

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем, авторизован ли пользователь
        if (!isUserLoggedIn()) {
            // Перенаправляем на экран авторизации
            startActivity(Intent(this, AuthActivity::class.java))
            finish() // Завершаем текущую активность, чтобы пользователь не мог вернуться сюда без авторизации
            return
        }

        // Настройка Google Sign-In клиента
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContentView(R.layout.activity_main)


        // Инициализация трекеров
        initTrackers()

        // Настройка кнопок
        setupButtons()

        // Проверка и запрос разрешений
        checkPermissions()


        // Проверка разрешения на статистику использования
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
        }

        // Настройка экранного времени
        screenTimeTextView = findViewById(R.id.btn_screen_time)
        startUpdatingScreenTime()

        collectData()

        startRepeatingTask()


    }

    private fun setupButtons() {
        findViewById<Button>(R.id.button_open_map).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        findViewById<Button>(R.id.button_request_notification_access).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.button_view_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<Button>(R.id.button_view_messages).setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            signOut()
        }

        findViewById<Button>(R.id.button_health).setOnClickListener {
            stepCounter.readSteps { steps ->
                heartRateMonitor.readHeartRate { heartRate ->
                    calorieTracker.readCalories { calories ->
                        distanceTracker.readDistance { distance ->
                            activeMinutesTracker.readActiveMinutes { activeMinutes ->
                                sleepTracker.readSleepData { sleepDuration ->
                                    val hours = TimeUnit.MILLISECONDS.toHours(sleepDuration)
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(sleepDuration) % 60
                                    val sleepTime = "$hours ч. $minutes мин."

                                    val intent = Intent(this, HealthActivity::class.java).apply {
                                        putExtra("EXTRA_STEPS", steps)
                                        putExtra("EXTRA_HEART_RATE", heartRate)
                                        putExtra("EXTRA_CALORIES", calories)
                                        putExtra("EXTRA_DISTANCE", distance)
                                        putExtra("EXTRA_ACTIVE_MINUTES", activeMinutes)
                                        putExtra("EXTRA_SLEEP_DURATION", sleepDuration)
                                    }
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }
        }

        findViewById<Button>(R.id.btn_call_logs).setOnClickListener {
            startActivity(Intent(this, CallLogActivity::class.java))
        }

        findViewById<Button>(R.id.btn_contacts).setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_app_usage).setOnClickListener {
            startActivity(Intent(this, AppUsageActivity::class.java))
        }
    }

    private fun collectData() {
        pendingOperations = 6

        // Заполнение данных о пользователе
        appData.userInfo = UserInfo(
            name = "Имя пользователя",   //  поправить чтобы брало из ауфактивити
            email = "user@example.com"   //
        )

        // Загрузка данных о здоровье
        stepCounter.readSteps { steps ->
            appData.healthData = appData.healthData?.copy(steps = steps)
                ?: HealthData(steps, emptyList(), 0f, emptyList(), 0f, emptyList(), 0f, emptyList(), 0, 0L)
            onOperationCompleted()
        }

        val dataManager = DataManager(this)
        val stepsHistory = dataManager.loadStepHistory() ?: emptyList()
        val heartRateHistory = dataManager.loadHeartRateHistory() ?: emptyList()
        val caloriesHistory = dataManager.loadCalorieHistory() ?: emptyList()
        val distanceHistory = dataManager.loadDistanceHistory() ?: emptyList()

        appData.healthData = appData.healthData?.copy(stepsHistory = stepsHistory)
            ?: HealthData(0, stepsHistory, 0f, emptyList(), 0f, emptyList(), 0f, emptyList(), 0, 0L)

        heartRateMonitor.readHeartRate { heartRate ->
            appData.healthData = appData.healthData?.copy(heartRate = heartRate)
                ?: HealthData(0, emptyList(), heartRate, emptyList(), 0f, emptyList(), 0f, emptyList(), 0, 0L)
            onOperationCompleted()
        }

        appData.healthData = appData.healthData?.copy(heartRateHistory = heartRateHistory)
            ?: HealthData(0, emptyList(), 0f, heartRateHistory, 0f, emptyList(), 0f, emptyList(), 0, 0L)

        calorieTracker.readCalories { calories ->
            appData.healthData = appData.healthData?.copy(calories = calories)
                ?: HealthData(0, emptyList(), 0f, emptyList(), calories, emptyList(), 0f, emptyList(), 0, 0L)
            onOperationCompleted()
        }

        appData.healthData = appData.healthData?.copy(caloriesHistory = caloriesHistory)
            ?: HealthData(0, emptyList(), 0f, emptyList(), 0f, caloriesHistory, 0f, emptyList(), 0, 0L)

        distanceTracker.readDistance { distance ->
            appData.healthData = appData.healthData?.copy(distance = distance)
                ?: HealthData(0, emptyList(), 0f, emptyList(), 0f, emptyList(), distance, emptyList(), 0, 0L)
            onOperationCompleted()
        }

        appData.healthData = appData.healthData?.copy(distanceHistory = distanceHistory)
            ?: HealthData(0, emptyList(), 0f, emptyList(), 0f, emptyList(), 0f, distanceHistory, 0, 0L)

        activeMinutesTracker.readActiveMinutes { activeMinutes ->
            appData.healthData = appData.healthData?.copy(activeMinutes = activeMinutes)
                ?: HealthData(0, emptyList(), 0f, emptyList(), 0f, emptyList(), 0f, emptyList(), activeMinutes, 0L)
            onOperationCompleted()
        }

        sleepTracker.readSleepData { sleepDuration ->
            appData.healthData = appData.healthData?.copy(sleepDuration = sleepDuration)
                ?: HealthData(0, emptyList(), 0f, emptyList(), 0f, emptyList(), 0f, emptyList(), 0, sleepDuration)
            onOperationCompleted()
        }

        //collectContacts()
        collectAppUsage()
        collectNotifications()
        collectSMS()
    }


    private fun exportToJson(): String {
        val gson = GsonBuilder().setPrettyPrinting().create()  // Создаем Gson с поддержкой красивого форматирования
        return gson.toJson(appData)  // Преобразуем объект appData в красивый JSON
    }



    private fun onOperationCompleted() {
        pendingOperations--
        if (pendingOperations == 0 && !isInitialDataSent) {
            if (!isInitialDelayStarted) {
                isInitialDelayStarted = true
                handler.postDelayed({
                    val jsonData = exportToJson()
                    authActivity.sendDataToServer(jsonData)
                    Log.d("JSON Data (Initial)", jsonData)
                    isInitialDataSent = true // Фиксируем отправку
                }, 15 * 1000L) // 15 секунд
            }
        }
    }


    private fun collectContacts() {
        // Получаем контакты с помощью метода getContacts()
        val contactsActivity = ContactsActivity()
        val contacts = contactsActivity.getContacts(this) // Передаем контекст текущей активности

        // Преобразуем список контактов в нужный формат для AppData
        appData.contacts = contacts.map { Contact(it.contact, it.number) }

        Log.d("MainActivity", "Contacts collected: ${appData.contacts.size}")
    }

    // Функция для сбора статистики использования приложений
    private fun collectAppUsage() {
        val appUsageActivity = AppUsageActivity()

        // Получаем данные о статистике использования приложений
        val appUsageStats = appUsageActivity.getAppUsageData(this)

        // Преобразуем данные в нужный формат для AppData
        appData.appUsage = appUsageStats.map { AppUsage(it.appName, it.timeInForeground) }

        Log.d("MainActivity", "App Usage collected: ${appData.appUsage.size}")
    }

    private fun collectCallLogs() { // этот метод работает скверно
        // Получаем экземпляр активности (контекст)
        val callLogActivity = CallLogActivity()

        // Проверяем разрешение на доступ к журналу звонков
        callLogActivity.checkCallLogPermission()

        // После того, как разрешение получено, можно вызвать метод для загрузки звонков
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            callLogActivity.loadCallLogs()  // Загружаем звонки
            appData.callLogs = callLogActivity.callLogs.map { CallLogData(it.number, it.date, it.type) }

            Log.d("MainActivity", "Call Logs collected: ${appData.callLogs.size}")
        }
    }

    private fun collectNotifications() {
        // Получаем уведомления с помощью метода getSavedNotifications
        val notifications = MyNotificationListener.getSavedNotifications(this)

        // Добавляем уведомления в appData
        appData.notifications = notifications

        Log.d("MainActivity", "Notifications collected: ${appData.notifications.size}")
    }

    private fun collectSMS() {
        // Получаем SMS с помощью метода getSavedSMS
        val smsList = MyNotificationListener.getSavedSMS(this)
        appData.sms = smsList
        Log.d("MainActivity", "SMS collected: ${appData.sms.size}")
    }




    private fun isUserLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null) // Сохраняем JWT токен
        return !token.isNullOrEmpty()
    }

    private fun signOut() {
        // Очистка токена из SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("jwt_token")
            apply()
        }

        // Выход из Google аккаунта
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // После выхода из аккаунта, перенаправляем на экран авторизации
            startActivity(Intent(this, AuthActivity::class.java))
            finish()  // Завершаем MainActivity
        }
    }



    private fun initTrackers() {
        stepCounter = StepCounter(this)
        heartRateMonitor = HeartRateMonitor(this)
        calorieTracker = CalorieTracker(this)
        distanceTracker = DistanceTracker(this)
        activeMinutesTracker = ActiveMinutesTracker(this)
        sleepTracker = SleepTracker(this)
    }


    private fun getScreenTime(): Long {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60 * 1000 // Последняя минута

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var screenOnTime = 0L
        var screenOnTimestamp = 0L

        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> screenOnTimestamp = event.timeStamp
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    if (screenOnTimestamp > 0) {
                        screenOnTime += event.timeStamp - screenOnTimestamp
                        screenOnTimestamp = 0L
                    }
                }
            }
        }
        return screenOnTime / 1000 / 60
    }

    private fun startUpdatingScreenTime() {
        updateHandler.post(object : Runnable {
            override fun run() {
                val screenTime = getScreenTime()
                val formattedTime = formatScreenTime(screenTime)
                runOnUiThread {
                    screenTimeTextView.text = "Экран активен: $formattedTime"
                }
                updateHandler.postDelayed(this, 60 * 1000L)
            }
        })
    }

    private fun formatScreenTime(screenTimeInMinutes: Long): String {
        val hours = screenTimeInMinutes / 60
        val minutes = screenTimeInMinutes % 60
        return if (hours > 0) "$hours ч. $minutes мин." else "$minutes мин."
    }



    private fun checkPermissions() {
        val permissions = listOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACTIVITY_RECOGNITION
        ).filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1001)
        }
    }


    private fun readDataFromGoogleFit() {
        if (isInitialDataSent) { // Проверяем, отправлены ли первичные данные
            val jsonData = exportToJson()
            Log.d("JSON Data (Periodic)", jsonData)
            authActivity.sendDataToServer(jsonData)
        } else {
            Log.d("readDataFromGoogleFit", "Первичная отправка данных еще не завершена, пропускаем")
        }
    }

    private val repeatingTask = object : Runnable {
        override fun run() {
            readDataFromGoogleFit()
            handler.postDelayed(this, updateInterval) // 5 минут
        }
    }
    private fun startRepeatingTask() {
        repeatingTask.run() // Первый запуск
    }

    private fun stopRepeatingTask() {
        handler.removeCallbacks(repeatingTask)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacksAndMessages(null)
        stopRepeatingTask()
    }
}
