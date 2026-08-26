package com.fiftycar.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * 小组件按钮广播接收 → 转交应用层执行控车指令
 * (小组件进程隔离,真正的执行走 CarRepository;此处演示入口,MVP 先 Toast 提示)
 */
class WidgetCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == QuickControlWidgetProvider.ACTION_WIDGET_CMD) {
            val cmd = intent.getStringExtra(QuickControlWidgetProvider.EXTRA_CMD) ?: return
            // TODO: 接入 CarRepository.executeCommand(CommandType.valueOf(cmd))
            Toast.makeText(context, "小组件指令: $cmd(待接入执行层)", Toast.LENGTH_SHORT).show()
        }
    }
}
