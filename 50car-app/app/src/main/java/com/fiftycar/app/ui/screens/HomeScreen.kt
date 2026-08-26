package com.fiftycar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fiftycar.app.ui.components.MqttStateChip
import com.fiftycar.app.ui.components.lastUpdatedText
import com.fiftycar.app.ui.vm.CarViewModel

/** 爱车页:状态总览(MD3 TopAppBar + 语义卡片 + 数据时间戳 + 连接态可见) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: CarViewModel, modifier: Modifier = Modifier) {
    val session by vm.session.collectAsState()
    val status by vm.status.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session.carName ?: "爱车") },
                actions = {
                    MqttStateChip(connected = status != null)
                    IconButton(onClick = { vm.refresh() }) { Icon(Icons.Filled.Refresh, "刷新") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(lastUpdatedText(status?.updatedAtEpochMs ?: 0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            // 电量主卡
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("电量 ${status?.batteryPercent ?: "--"}%",
                        style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (status?.batteryPercent ?: 0) / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("续航约 ${status?.rangeKm ?: "--"} km · 总里程 ${status?.totalMileageKm ?: "--"} km",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }

            // 车锁/车窗/空调/充电状态行
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatusPill("车锁", if (status?.locked == true) "已锁" else "未锁", status?.locked != true)
                    StatusPill("车窗", if (status?.windowsOpen == true) "开启" else "关闭", status?.windowsOpen == true)
                    StatusPill("空调", if (status?.acOn == true) "运行" else "关闭", false)
                    StatusPill("充电", if (status?.charging == true) "进行中" else "未充电", false)
                }
            }

            // 车内温度 & 胎压
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ElevatedCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.DeviceThermostat, null,
                            tint = MaterialTheme.colorScheme.primary)
                        Text("车内温度", style = MaterialTheme.typography.labelMedium)
                        Text("${status?.innerTempC ?: "--"} ℃", style = MaterialTheme.typography.titleLarge)
                    }
                }
                ElevatedCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) {
                        Icon(Icons.Filled.Speed, null, tint = MaterialTheme.colorScheme.primary)
                        Text("胎压 (bar)", style = MaterialTheme.typography.labelMedium)
                        Text(status?.tirePressures?.joinToString("  ") { "%.1f".format(it) } ?: "--",
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, value: String, warn: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall,
            color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}
