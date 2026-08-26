package com.fiftycar.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** 开机恢复保活(Android 12+ 不能直接拉 FGS 除外情形:BOOT_COMPLETED 允许 dataSync FGS 拉起) */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(Intent(context, KeepAliveService::class.java))
        }
    }
}
