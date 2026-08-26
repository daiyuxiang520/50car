package com.fiftycar.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fiftycar.app.ui.vm.CarViewModel

/** 我的:账号、保活引导(原 50car 的存活依赖这些设置但从来不引导)、关于 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: CarViewModel) {
    val context = LocalContext.current
    val pm = context.getSystemService(android.os.PowerManager::class.java)
    val pkg = context.packageName
    val ignored = remember { pm?.isIgnoringBatteryOptimizations(pkg) == true }

    Scaffold(topBar = { TopAppBar(title = { Text("我的") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            // ★ 保活引导:MQTT 常驻需要电池优化白名单(车厂 ROM 特别重要)
            ListItem(
                headlineContent = { Text("后台保活白名单") },
                supportingContent = {
                    Text(if (ignored) "已加入电池优化白名单,车联保活可常驻"
                    else "未加入——可能收不到车辆状态推送,点我引导设置")
                },
                leadingContent = {
                    Icon(Icons.Filled.BatterySaver, null,
                        tint = if (ignored) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.clickable {
                    if (!ignored) {
                        // 用系统设置页引导(不需要 REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 权限)
                        context.startActivity(
                            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                },
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))

            // Android 13+ 通知运行时权限引导(Android 16 依然需要)
            ListItem(
                headlineContent = { Text("通知权限") },
                supportingContent = { Text("电量/胎压/亏电等提醒需要通知权限") },
                leadingContent = { Icon(Icons.Filled.Notifications, null) },
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("退出登录") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, null,
                    tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { vm.logout() },
            )

            Text(
                "50Car 车控(社区版) v1.0.0 · targetSdk 36(Android 16)\n" +
                    "原创开源工程,不含任何第三方私有密钥;车控能力通过自有授权凭据接入",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
