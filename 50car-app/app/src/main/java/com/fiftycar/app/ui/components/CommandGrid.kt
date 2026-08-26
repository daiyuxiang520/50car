package com.fiftycar.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fiftycar.app.data.CommandState
import com.fiftycar.app.data.CommandType

/** 控车按钮(MD3 ElevatedCard + 按风险给语义色) */
@Composable
fun CommandButton(
    label: String,
    icon: ImageVector,
    cmd: CommandType,
    executing: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (CommandType) -> Unit,
) {
    val tint = when (cmd.riskLevel) {
        CommandType.Risk.HIGH -> MaterialTheme.colorScheme.error          // 解锁/后备箱/启动
        CommandType.Risk.MEDIUM -> MaterialTheme.colorScheme.tertiary     // 空调/车窗
        CommandType.Risk.LOW -> MaterialTheme.colorScheme.primary
    }
    ElevatedCard(
        onClick = { onClick(cmd) },
        enabled = enabled && !executing,
        modifier = modifier.height(88.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = label, tint = tint)
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
            if (executing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
        }
    }
}

/** 防误触分级确认:LOW 直接执行;MEDIUM/HIGH 弹出确认并带场景化警示 */
fun confirmTextFor(cmd: CommandType): String? = when (cmd) {
    CommandType.UNLOCK -> "确认车辆周边安全\n远程解锁可能被他人利用,请确认车辆处于可视范围或安全环境。"
    CommandType.TAILGATE -> "确认车辆周边安全\n后备箱开启后车内财物存在被盗风险。"
    CommandType.IGNITION_AUTH -> "确认车辆处于安全状态\n远程启动前请确认车周无障碍、档位正确。"
    CommandType.AC_ON -> "确认车辆处于室外通风环境\n严禁在密闭车库/隧道内长时间远程开空调,谨防一氧化碳积聚!"
    else -> null
}

@Composable
fun ConfirmCommandDialog(
    cmd: CommandType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(confirmTextFor(cmd)?.substringBefore('\n') ?: "确认执行?") },
        text = { Text(confirmTextFor(cmd)?.substringAfter('\n') ?: "") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认执行", color = if (cmd.riskLevel == CommandType.Risk.HIGH)
                    MaterialTheme.colorScheme.error else Color.Unspecified)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
fun CommandResultDialog(state: CommandState, onDismiss: () -> Unit) {
    val (title, body) = when (state) {
        is CommandState.Acked -> "执行成功" to state.message
        is CommandState.Timeout -> "响应超时" to state.message
        is CommandState.Failed -> "执行失败" to state.cause
        else -> return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } },
    )
}
