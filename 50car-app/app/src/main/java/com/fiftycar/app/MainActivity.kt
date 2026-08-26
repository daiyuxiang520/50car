package com.fiftycar.app

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fiftycar.app.ui.AppRoot
import com.fiftycar.app.ui.theme.FiftyCarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ★ Android 16:edge-to-edge 对 targetSdk36 强制且不可关闭,
        //   必须自行处理状态栏/导航栏 insets(见各 Screen 的 Scaffold)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FiftyCarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(nfcAction = parseNfcIntent(intent))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // NFC 标签唤起:负载约定(兼容原 50car 实体卡格式)
        //   action:xxx;vin:xxx;temperature:xx
    }

    /** 解析 NDEF 控车卡(负载:"action:xxx;vin:xxx;temperature:xx") */
    private fun parseNfcIntent(intent: Intent?): NfcCarAction? {
        if (intent == null) return null
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED) return null
        val msgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            ?.filterIsInstance<NdefMessage>() ?: return null
        val payload = msgs.firstOrNull()?.records?.firstOrNull()?.payload ?: return null
        val text = runCatching { String(payload, Charsets.UTF_8) }.getOrNull() ?: return null
        val map = text.split(';').mapNotNull {
            val idx = it.indexOf(':'); if (idx <= 0) null else it.substring(0, idx) to it.substring(idx + 1)
        }.toMap()
        val action = map["action"] ?: return null
        return NfcCarAction(action, map["vin"], map["temperature"]?.toIntOrNull())
    }
}

/** NFC 卡携带的控车指令 */
data class NfcCarAction(val action: String, val vin: String?, val temperature: Int?)
