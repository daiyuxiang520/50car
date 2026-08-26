package com.fiftycar.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** MQTT 连接状态可视化(原 50car 只在日志里,用户不可见) */
@Composable
fun MqttStateChip(connected: Boolean, modified: Boolean = false) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                if (connected) "车联已连接"
                else "车联重连中…",
                color = if (connected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
            )
        },
    )
}

/** ★ 数据时间戳(解决"状态不知是何时刷新"的痛点) */
@Composable
fun lastUpdatedText(epochMs: Long): String = if (epochMs <= 0) "暂无数据"
else "更新于 " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(epochMs))
