package com.fiftycar.app.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class CarSession(
    val token: String? = null,
    val vin: String? = null,
    val carName: String? = null,
)

/**
 * 车辆仓库:统一数据入口
 * - 登录态持久化(TokenStore)+ token 失效广播(原 50car 只弹 Toast)
 * - 指令状态机暴露给 UI(下发→回执→超时)
 */
@Singleton
class CarRepository @Inject constructor(
    private val api: CarApi,
    private val tokenStore: TokenStore,
) {
    private val mutex = Mutex()

    private val _session = MutableStateFlow(CarSession())
    val session: StateFlow<CarSession> = _session

    private val _status = MutableStateFlow<VehicleStatus?>(null)
    val status: StateFlow<VehicleStatus?> = _status

    private val _commandState = MutableStateFlow<Pair<CommandType, CommandState>?>(null)
    val commandState: StateFlow<Pair<CommandType, CommandState>?> = _commandState

    private val _tokenInvalid = MutableStateFlow(false)
    val tokenInvalid: StateFlow<Boolean> = _tokenInvalid

    suspend fun restoreSession() {
        val token = tokenStore.tokenFlow.first()
        val vin = tokenStore.vinFlow.first()
        if (token != null) {
            _session.value = CarSession(token = token, vin = vin)
            refreshCars()
        }
    }

    suspend fun login(username: String, password: String): Result<Unit> = mutex.withLock {
        api.loginAccount(username, password)
            .onSuccess { token ->
                _session.value = CarSession(token = token)
                refreshCars()
            }
            .map { }
    }

    suspend fun refreshCars() {
        val token = _session.value.token ?: return
        api.queryCars(token).onSuccess { cars ->
            val current = cars.firstOrNull()
            _session.value = _session.value.copy(vin = current?.vin, carName = current?.name)
            tokenStore.save(token, current?.vin)
            current?.let { refreshStatus() }
        }.onFailure { if (it.message?.contains("401") == true) _tokenInvalid.value = true }
    }

    suspend fun refreshStatus() {
        val s = _session.value; val token = s.token ?: return; val vin = s.vin ?: return
        api.queryStatus(token, vin)
            .onSuccess { _status.value = it }
            .onFailure { if (it.message?.contains("401") == true) _tokenInvalid.value = true }
    }

    /** 指令闭环:发送→等待执行→刷新状态回执;超时由 UI 侧 withTimeout 兜底 */
    suspend fun executeCommand(cmd: CommandType, temperature: Int? = null) {
        val s = _session.value; val token = s.token ?: return; val vin = s.vin ?: return
        _commandState.value = cmd to CommandState.Sending
        api.sendCommand(token, vin, cmd, temperature)
            .onSuccess {
                _commandState.value = cmd to CommandState.Acked(it)
                delay(200)      // 给车端状态上报一点到达时间
                refreshStatus()
            }
            .onFailure {
                _commandState.value = cmd to CommandState.Failed(it.message ?: "执行失败")
            }
    }

    suspend fun logout() {
        _session.value = CarSession(); _status.value = null
        tokenStore.clear()
    }
}
