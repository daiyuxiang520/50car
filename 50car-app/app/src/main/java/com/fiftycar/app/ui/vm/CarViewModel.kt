package com.fiftycar.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiftycar.app.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarViewModel @Inject constructor(
    private val repo: CarRepository,
) : ViewModel() {

    val session: StateFlow<CarSession> = repo.session
    val status: StateFlow<VehicleStatus?> = repo.status
    val tokenInvalid: StateFlow<Boolean> = repo.tokenInvalid

    private val _command = MutableStateFlow<Pair<CommandType, CommandState>?>(null)
    val command: StateFlow<Pair<CommandType, CommandState>?> = _command

    private val _loggingIn = MutableStateFlow(false)
    val loggingIn: StateFlow<Boolean> = _loggingIn
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private var timeoutJob: Job? = null

    init {
        viewModelScope.launch { repo.restoreSession() }
        // 转发仓库指令状态
        viewModelScope.launch { repo.commandState.collect { _command.value = it } }
    }

    fun login(user: String, pwd: String) = viewModelScope.launch {
        _loggingIn.value = true; _loginError.value = null
        repo.login(user, pwd).onFailure { _loginError.value = it.message ?: "登录失败" }
        _loggingIn.value = false
    }

    fun logout() = viewModelScope.launch { repo.logout() }

    fun refresh() = viewModelScope.launch { repo.refreshStatus() }

    /**
     * 执行控车指令(带 25s 超时回滚,原 50car 缺失的闭环体验)
     * 高风险确认在 UI 层完成(ConfirmCommandDialog)
     */
    fun executeCommand(cmd: CommandType, temperature: Int? = null) {
        timeoutJob?.cancel()
        viewModelScope.launch { repo.executeCommand(cmd, temperature) }
        timeoutJob = viewModelScope.launch {
            delay(25_000)
            if (_command.value?.first == cmd && _command.value?.second is CommandState.Sending) {
                _command.value = cmd to CommandState.Timeout("车辆响应超时,请检查网络后重试")
            }
        }
    }

    fun dismissCommandResult() { _command.value = null }
    fun clearTokenInvalid() = viewModelScope.launch { repo.logout() }
    fun handleNfcAction(action: String, temperature: Int?) {
        val cmd = runCatching { CommandType.valueOf(action.uppercase()) }.getOrNull() ?: return
        executeCommand(cmd, temperature)
    }
}
