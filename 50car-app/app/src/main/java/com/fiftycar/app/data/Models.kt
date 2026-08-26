package com.fiftycar.app.data

/** 车辆基础信息 */
data class CarInfo(
    val vin: String,
    val name: String,
    val plate: String = "",
    val colorCode: String = "",
)

/** 车辆实时状态(对原 50car 状态域的抽象) */
data class VehicleStatus(
    val batteryPercent: Int = 0,        // 电量 %
    val rangeKm: Int = 0,               // 剩余续航
    val totalMileageKm: Int = 0,        // 总里程
    val innerTempC: Double? = null,     // 车内温度
    val tirePressures: List<Double> = emptyList(), // 四轮胎压 bar
    val locked: Boolean = true,
    val windowsOpen: Boolean = false,
    val acOn: Boolean = false,
    val charging: Boolean = false,
    val updatedAtEpochMs: Long = 0L,    // ★ 数据时间戳(原 50car 痛点:状态无"最后更新于")
)

/** 控车指令类型(高风险指令需二次确认,见 riskLevel) */
enum class CommandType(val riskLevel: Risk) {
    LOCK(Risk.LOW),
    UNLOCK(Risk.HIGH),          // 解锁→防盗风险,需二次确认
    WINDOW_OPEN(Risk.MEDIUM),
    WINDOW_CLOSE(Risk.LOW),
    AC_ON(Risk.MEDIUM),         // 密闭空间一氧化碳风险提示
    AC_OFF(Risk.LOW),
    TAILGATE(Risk.HIGH),
    FIND_CAR(Risk.LOW),
    IGNITION_AUTH(Risk.HIGH),   // 远程启动授权
    CHARGE_START(Risk.LOW),
    CHARGE_STOP(Risk.LOW),
    ;

    enum class Risk { LOW, MEDIUM, HIGH }
}

/** 指令执行状态(原 50car 只有 HTTP 重试,没有指令级闭环) */
sealed interface CommandState {
    data object Idle : CommandState
    data object Sending : CommandState                 // 已下发,等待车端回执
    data class Acked(val message: String) : CommandState      // MQTT status topic 回执确认
    data class Timeout(val message: String) : CommandState    // 超时未回执→UI 回滚并提示重试
    data class Failed(val cause: String) : CommandState
}

/** 预约充电 */
data class ChargeReservation(
    val id: String,
    val startHour: Int,
    val startMinute: Int,
    val repeatDaily: Boolean,
    val enabled: Boolean,
)
