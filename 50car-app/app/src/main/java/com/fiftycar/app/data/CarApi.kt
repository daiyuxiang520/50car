package com.fiftycar.app.data

/**
 * 车控能力抽象层
 *
 * ⚠️ 合规说明:
 *  - 本工程【不包含】任何第三方/官方应用的私有密钥、签名或访问令牌。
 *  - 若要对​接真实车辆,请接入你自己拥有授权的凭据或自有后端,
 *    实现本接口替换 MockCarApi 即可(di/AppModule.kt 中换绑)。
 *  - 盗用官方 APP 提取的密钥调用其私有接口存在法律与账号封禁风险,请勿如此。
 */
interface CarApi {

    suspend fun loginAccount(username: String, password: String): Result<String> // 返回 accessToken

    suspend fun queryCars(token: String): Result<List<CarInfo>>

    suspend fun queryStatus(token: String, vin: String): Result<VehicleStatus>

    suspend fun sendCommand(token: String, vin: String, cmd: CommandType,
                            temperature: Int? = null): Result<String>

    /** MQTT 凭据(动态,原 50car 走 getMqttCredentials) */
    suspend fun fetchMqttCredentials(token: String, vin: String): Result<MqttCredentials>

    suspend fun queryChargeReservations(token: String, vin: String): Result<List<ChargeReservation>>

    suspend fun saveChargeReservation(token: String, vin: String, r: ChargeReservation): Result<Unit>
}

data class MqttCredentials(
    val brokerUrl: String,
    val username: String,
    val password: String,
    val clientId: String,
    val topics: List<String>,
)
