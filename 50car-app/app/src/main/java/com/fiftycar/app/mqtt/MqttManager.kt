package com.fiftycar.app.mqtt

import com.fiftycar.app.data.MqttCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.inject.Inject
import javax.inject.Singleton

enum class MqttState { DISCONNECTED, CONNECTING, CONNECTED }

/**
 * 车联 MQTT 通道(连接状态对用户可见,自动重连退避可配置)
 * ⚠️ 生产环境强烈建议使用 ssl:// 而非 tcp://(原 50car broker 为明文 1883)
 */
@Singleton
class MqttManager @Inject constructor() {

    private var client: MqttAsyncClient? = null

    private val _state = MutableStateFlow(MqttState.DISCONNECTED)
    val state: StateFlow<MqttState> = _state

    private val _vehicleMessages = MutableStateFlow<Pair<String, String>?>(null) // topic -> payload
    val vehicleMessages: StateFlow<Pair<String, String>?> = _vehicleMessages

    @Volatile private var manualDisconnect = false

    @Synchronized
    fun connect(cred: MqttCredentials) {
        if (client?.isConnected == true) return
        manualDisconnect = false
        _state.value = MqttState.CONNECTING
        val c = MqttAsyncClient(cred.brokerUrl, cred.clientId, MemoryPersistence())
        client = c
        c.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                _state.value = MqttState.DISCONNECTED
                if (!manualDisconnect) scheduleReconnect(cred)
            }
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic != null && message != null)
                    _vehicleMessages.value = topic to String(message.payload, Charsets.UTF_8)
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
        val opts = MqttConnectOptions().apply {
            userName = cred.username
            password = cred.password.toCharArray()
            isAutomaticReconnect = true     // Paho 内置重连;外层退避做兜底
            isCleanSession = false          // 保持订阅,弱网恢复不丢消息
            connectionTimeout = 10
            keepAliveInterval = 60
        }
        c.connect(opts, null, object : org.eclipse.paho.client.mqttv3.IMqttActionListener {
            override fun onSuccess(asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?) {
                _state.value = MqttState.CONNECTED
                cred.topics.forEach { t -> c.subscribe(t, 1) } // QoS1,与原车联通道一致
            }
            override fun onFailure(asyncActionToken: org.eclipse.paho.client.mqttv3.IMqttToken?, exception: Throwable?) {
                _state.value = MqttState.DISCONNECTED
                scheduleReconnect(cred)
            }
        })
    }

    /** 指数退避重连:1s→2s→…→上限 5min */
    private fun scheduleReconnect(cred: MqttCredentials) {
        Thread {
            var delayMs = 1_000L
            while (!manualDisconnect && client?.isConnected != true) {
                Thread.sleep(delayMs)
                if (manualDisconnect) break
                runCatching { connect(cred) }
                delayMs = (delayMs * 2).coerceAtMost(300_000L)
            }
        }.start()
    }

    fun publish(topic: String, payload: String) {
        client?.takeIf { it.isConnected }?.publish(topic, payload.toByteArray(Charsets.UTF_8), 1, false)
    }

    @Synchronized
    fun disconnect() {
        manualDisconnect = true
        client?.disconnect()
        client = null
        _state.value = MqttState.DISCONNECTED
    }
}
