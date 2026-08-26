package com.fiftycar.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.fiftycar.app.NfcCarAction
import com.fiftycar.app.ui.screens.ChargeScreen
import com.fiftycar.app.ui.screens.ControlScreen
import com.fiftycar.app.ui.screens.HomeScreen
import com.fiftycar.app.ui.screens.LoginScreen
import com.fiftycar.app.ui.screens.ProfileScreen
import com.fiftycar.app.ui.vm.CarViewModel

enum class MainTab { HOME, CONTROL, CHARGE, PROFILE }

/**
 * 根界面:NavigationSuiteScaffold 自适应布局
 * - 手机竖屏=底部 NavigationBar,横屏/平板/车机=侧边 NavigationRail(MD3 自适应,Android 16 大屏规范)
 * - token 失效自动回登录页(原 50car 只 Toast)
 */
@Composable
fun AppRoot(
    nfcAction: NfcCarAction? = null,
    vm: CarViewModel = hiltViewModel(),
) {
    val session by vm.session.collectAsState()
    val tokenInvalid by vm.tokenInvalid.collectAsState()

    if (session.token == null || tokenInvalid) {
        LoginScreen(vm)
        return
    }

    // NFC 实体卡直达控车(免开屏操作);只执行一次,避免重组重复下发
    androidx.compose.runtime.LaunchedEffect(nfcAction) {
        if (nfcAction != null) {
            vm.handleNfcAction(nfcAction.action, nfcAction.temperature)
        }
    }

    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = tab == MainTab.HOME, onClick = { tab = MainTab.HOME },
                icon = { Icon(Icons.Filled.DirectionsCar, "爱车") }, label = { Text("爱车") },
            )
            item(
                selected = tab == MainTab.CONTROL, onClick = { tab = MainTab.CONTROL },
                icon = { Icon(Icons.Filled.Tune, "控制") }, label = { Text("控制") },
            )
            item(
                selected = tab == MainTab.CHARGE, onClick = { tab = MainTab.CHARGE },
                icon = { Icon(Icons.Filled.BatteryChargingFull, "充电") }, label = { Text("充电") },
            )
            item(
                selected = tab == MainTab.PROFILE, onClick = { tab = MainTab.PROFILE },
                icon = { Icon(Icons.Filled.Person, "我的") }, label = { Text("我的") },
            )
        },
    ) {
        when (tab) {
            MainTab.HOME -> HomeScreen(vm, Modifier.padding())
            MainTab.CONTROL -> ControlScreen(vm)
            MainTab.CHARGE -> ChargeScreen(vm)
            MainTab.PROFILE -> ProfileScreen(vm)
        }
    }
}
