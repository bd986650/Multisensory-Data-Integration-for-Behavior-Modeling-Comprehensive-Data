package com.example.app

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AppUsageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_usage)
    }

    // Функция для получения списка статистики использования приложений
    fun getAppUsageData(context: Context): List<AppUsage> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000 // последние 24 часа

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val appUsageList = mutableListOf<AppUsage>()
        usageStatsList.forEach { usageStats ->
            val packageName = usageStats.packageName
            val appName = try {
                context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0))
            } catch (e: PackageManager.NameNotFoundException) {
                packageName // Если не удалось получить имя, выводим packageName
            }
            val totalTimeInForeground = usageStats.totalTimeInForeground / 1000 // в секундах

            if (totalTimeInForeground > 0) {
                appUsageList.add(AppUsage(appName.toString(), totalTimeInForeground))
            }
        }

        return appUsageList
    }
}
