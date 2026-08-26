package com.fiftycar.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fiftycar.app.R

/**
 * 车联保活前台服务
 * ★ Android 14+ 强制:启动 FGS 必须带 foregroundServiceType(manifest 已声明 dataSync)
 * ★ Android 16 强化了"用户可视"要求:MIN 通知级别 + 明确文案,不做隐藏保活(会被系统杀)
 */
class KeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // API34+
            startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(ID, notification)
        }
        return START_STICKY   // 被杀后自动拉起
    }

    private fun buildNotification(): Notification {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, getString(R.string.channel_keep_alive))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("车联保活中")
            .setContentText("保持车辆状态推送连接")
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object { private const val ID = 1001 }
}
