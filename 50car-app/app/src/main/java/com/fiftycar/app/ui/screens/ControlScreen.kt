package com.fiftycar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fiftycar.app.data.CommandState
import com.fiftycar.app.data.CommandType
import com.fiftycar.app.ui.components.CommandButton
import com.fiftycar.app.ui.components.CommandResultDialog
import com.fiftycar.app.ui.components.ConfirmCommandDialog
import com.fiftycar.app.ui.vm.CarViewModel

/** 控制页:九宫格控车 + 风险分级二次确认 + 指令闭环反馈 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(vm: CarViewModel) {
    val status by vm.status.collectAsState()
    val command by vm.command.collectAsState()
    var pendingConfirm by remember { mutableStateOf<CommandType?>(null) }

    val onCommandClick: (CommandType) -> Unit = { cmd ->
        if (cmd.riskLevel == CommandType.Risk.LOW) vm.executeCommand(cmd) else pendingConfirm = cmd
    }

    pendingConfirm?.let { cmd ->
        ConfirmCommandDialog(
            cmd = cmd,
            onConfirm = { pendingConfirm = null; vm.executeCommand(cmd) },
            onDismiss = { pendingConfirm = null },
        )
    }
    command?.let { (cmd, state) ->
        if (state !is CommandState.Idle && state !is CommandState.Sending) {
            CommandResultDialog(state, onDismiss = { vm.dismissCommandResult() })
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("远程控制") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("高风险操作(解锁/后备箱/启动)需二次确认", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            val items1 = listOf(
                Triple(if (status?.locked == true) "解锁" else "锁车",
                    if (status?.locked == true) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    if (status?.locked == true) CommandType.UNLOCK else CommandType.LOCK),
                Triple("后备箱", Icons.Filled.MeetingRoom, CommandType.TAILGATE),
                Triple(if (status?.windowsOpen == true) "关窗" else "开窗",
                    Icons.Filled.Window,
                    if (status?.windowsOpen == true) CommandType.WINDOW_CLOSE else CommandType.WINDOW_OPEN),
            )
            val items2 = listOf(
                Triple(if (status?.acOn == true) "关空调" else "开空调",
                    Icons.Filled.AcUnit,
                    if (status?.acOn == true) CommandType.AC_OFF else CommandType.AC_ON),
                Triple("远程启动", Icons.Filled.PowerSettingsNew, CommandType.IGNITION_AUTH),
                Triple("鸣笛寻车", Icons.Filled.Search, CommandType.FIND_CAR),
            )
            listOf(items1, items2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (label, icon, cmd) ->
                        CommandButton(
                            label = label, icon = icon, cmd = cmd,
                            executing = command?.first == cmd && command?.second is CommandState.Sending,
                            modifier = Modifier.weight(1f),
                            onClick = onCommandClick,
                        )
                    }
                }
            }
        }
    }
}
