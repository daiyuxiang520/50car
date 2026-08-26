package com.fiftycar.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FiftyCarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        // WorkManager 周期任务兜底 MQTT 保活(Doze/厂商杀后台场景)
        // Android 16 仍遵循最短 15 分钟周期约束
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                getString(R.string.channel_keep_alive),
                getString(R.string.channel_keep_alive),
                NotificationManager.IMPORTANCE_MIN
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                getString(R.string.channel_car_alert),
                getString(R.string.channel_car_alert),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }
}
