package com.fiftycar.app.data

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/** 演示用 Mock 实现:离线可跑通全部交互;真实实现替换此绑定即可 */
@Singleton
class MockCarApi @Inject constructor() : CarApi {

    private var status = VehicleStatus(
        batteryPercent = 62, rangeKm = 238, totalMileageKm = 12340,
        innerTempC = 31.5, tirePressures = listOf(2.4, 2.4, 2.5, 2.3),
        locked = true, windowsOpen = false, acOn = false, charging = false,
        updatedAtEpochMs = System.currentTimeMillis(),
    )

    override suspend fun loginAccount(username: String, password: String): Result<String> {
        delay(600)
        return if (username.isNotBlank() && password.length >= 6)
            Result.success("mock_token_${System.currentTimeMillis()}")
        else Result.failure(IllegalArgumentException("账号或密码格式不正确"))
    }

    override suspend fun queryCars(token: String): Result<List<CarInfo>> {
        delay(300)
        return Result.success(listOf(CarInfo(vin = "LK6ADEMOCK000001", name = "五菱缤果", plate = "桂B·D88888")))
    }

    override suspend fun queryStatus(token: String, vin: String): Result<VehicleStatus> {
        delay(400)
        return Result.success(status.copy(updatedAtEpochMs = System.currentTimeMillis()))
    }

    override suspend fun sendCommand(token: String, vin: String, cmd: CommandType,
                                     temperature: Int?): Result<String> {
        delay(900) // 模拟车端执行时延
        status = when (cmd) {
            CommandType.LOCK -> status.copy(locked = true)
            CommandType.UNLOCK -> status.copy(locked = false)
            CommandType.WINDOW_OPEN -> status.copy(windowsOpen = true)
            CommandType.WINDOW_CLOSE -> status.copy(windowsOpen = false)
            CommandType.AC_ON -> status.copy(acOn = true)
            CommandType.AC_OFF -> status.copy(acOn = false)
            CommandType.CHARGE_START -> status.copy(charging = true)
            CommandType.CHARGE_STOP -> status.copy(charging = false)
            CommandType.TAILGATE, CommandType.FIND_CAR, CommandType.IGNITION_AUTH -> status
        }.copy(updatedAtEpochMs = System.currentTimeMillis())
        return Result.success("指令已执行")
    }

    override suspend fun fetchMqttCredentials(token: String, vin: String): Result<MqttCredentials> {
        delay(300)
        return Result.success(
            MqttCredentials(
                brokerUrl = "tcp://mqtt.example.com:1883", // TODO 换成你自己的 broker
                username = "demo", password = "demo",
                clientId = "${vin}_0000",
                topics = listOf("$vin/prod/sgmw/vehicle/status/business"),
            )
        )
    }

    override suspend fun queryChargeReservations(token: String, vin: String) =
        Result.success(listOf(ChargeReservation("r1", 23, 30, repeatDaily = true, enabled = true)))

    override suspend fun saveChargeReservation(token: String, vin: String, r: ChargeReservation) =
        Result.success(Unit)
}
