package com.fiftycar.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fiftycar.app.ui.vm.CarViewModel

@Composable
fun LoginScreen(vm: CarViewModel) {
    var user by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    val logging by vm.loggingIn.collectAsState()
    val error by vm.loginError.collectAsState()

    // ★ edge-to-edge 下避让系统栏(Android 16 强制全屏,必须处理)
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Filled.DirectionsCar, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(72.dp))
        Spacer(Modifier.height(8.dp))
        Text("50Car 车控(社区版)", style = MaterialTheme.typography.headlineSmall)
        Text("请使用你自己的账号凭据登录", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(user, { user = it }, label = { Text("手机号/账号") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pwd, { pwd = it }, label = { Text("密码") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth())
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.login(user.trim(), pwd) },
            enabled = !logging && user.isNotBlank() && pwd.length >= 6,
            modifier = Modifier.fillMaxWidth().height(48.dp), // ★ 触摸目标 ≥48dp(MD3 a11y)
        ) {
            if (logging) CircularProgressIndicator(modifier = Modifier.height(20.dp)) else Text("登录")
        }
        Spacer(Modifier.height(12.dp))
        Text("提示:演示模式输入任意账号 + ≥6 位密码即可进入",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
