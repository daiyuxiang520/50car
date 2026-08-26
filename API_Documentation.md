# 五菱汽车 API 接口文档

## 一、基础配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| **API Base URL** | `https://openapi.baojun.net/junApi/sgmw` | API 服务器地址 |
| **App Code** | `sgmw_llb` | 应用标识码 |
| **App Version** | `1691` | 应用版本号 |
| **System** | `android` | 操作系统类型 |
| **System Version** | `10` | 系统版本 |
| **APP Version** | `V8.2.17` | 客户端版本 |
| **User Agent** | `okhttp/4.9.0` | HTTP客户端标识 |
| **Channel** | `linglingbang` | 渠道标识 |
| **Platform No** | `Android` | 平台编号 |
| **Device Model** | `MI 8` | 设备型号 |
| **Device Brand** | `Xiaomi` | 设备品牌 |
| **Device Type** | `Android` | 设备类型 |
| **Access Channel** | `1` | 接入渠道 |
| **IMEI** | `a-c62b2f538bf34758` | 设备IMEI（模拟） |

---

## 二、认证与签名机制

### 2.1 认证流程

```
1. 用户登录 → 获取 accessToken、clientId、clientSecret
2. 每次请求 → 使用以上凭证生成签名
3. 服务器验证签名 → 返回数据
```

### 2.2 凭证存储

| 字段 | 存储位置 | 说明 |
|------|----------|------|
| `accessToken` | SharedPreferences (`wuling_auth`) | 访问令牌 |
| `clientId` | SharedPreferences (`wuling_auth`) | 客户端ID |
| `clientSecret` | SharedPreferences (`wuling_auth`) | 客户端密钥 |

### 2.3 签名算法

```kotlin
// 签名字符串拼接顺序
signStr = accessToken + 
          timestamp +      // 当前时间戳（毫秒）
          nonce +          // 10位随机字母
          clientId + 
          clientSecret + 
          appCode + 
          appVersion + 
          system + 
          systemVersion

// 签名方式
signature = SHA-256(signStr).toLowerCase()
```

### 2.4 请求头参数

| Header 名称 | 说明 | 示例值 |
|-------------|------|--------|
| `Accept` | 接受类型 | `application/json` |
| `Content-Type` | 内容类型 | `application/json; charset=UTF-8` |
| `User-Agent` | 客户端标识 | `okhttp/4.9.0` |
| `channel` | 渠道 | `linglingbang` |
| `platformNo` | 平台编号 | `Android` |
| `appVersionCode` | 应用版本代码 | `1691` |
| `version` | 版本号 | `V8.2.17` |
| `imei` | 设备标识 | `a-c62b2f538bf34758` |
| `deviceModel` | 设备型号 | `MI 8` |
| `deviceBrand` | 设备品牌 | `Xiaomi` |
| `deviceType` | 设备类型 | `Android` |
| `accessChannel` | 接入渠道 | `1` |
| **`sgmwaccesstoken`** | 访问令牌 | 登录获取 |
| **`sgmwtimestamp`** | 请求时间戳 | 当前毫秒时间 |
| **`sgmwnonce`** | 随机数 | 10位随机字母 |
| **`sgmwclientid`** | 客户端ID | 登录获取 |
| **`sgmwclientsecret`** | 客户端密钥 | 登录获取 |
| `sgmwappcode` | 应用代码 | `sgmw_llb` |
| `sgmwappversion` | 应用版本 | `1691` |
| `sgmwsystem` | 系统类型 | `android` |
| `sgmwsystemversion` | 系统版本 | `10` |
| **`sgmwsignature`** | 签名值 | SHA-256 哈希 |

---

## 三、API 接口列表

### 3.1 用户相关接口

| 接口路径 | 方法 | 功能描述 | 请求参数 | 返回数据 |
|----------|------|----------|----------|----------|
| `user/login` | POST | 用户登录 | `phone`, `password`, `appCode`, `appVersion`, `system`, `systemVersion` | `accessToken`, `clientId`, `clientSecret`, `phone` |
| `user/info` | POST | 获取用户信息 | 无 | `phone`, `nickname`, `avatar`, `openId` |
| `userCarRelation/queryCarList` | POST | 查询车辆列表 | 无 | `carList` 数组（含 `vin`, `carName`, `model`, `isDefault`） |
| `userCarRelation/queryDefaultCarStatus` | POST | 查询默认车辆状态 | 无 | `carStatus` 对象、`carInfo` 对象 |

### 3.2 车辆状态查询

| 接口路径 | 方法 | 功能描述 | 请求参数 | 返回数据 |
|----------|------|----------|----------|----------|
| `car/check/all` | POST | 查询车辆全量状态 | `vin` | 完整车辆状态对象 |
| `car/info/tire/pressure` | POST | 查询胎压信息 | `vin` | 胎压数据 |
| `car/yesterday/mileage` | POST | 查询昨日里程 | `vin` | 里程数据 |
| `car/control/ble/key/query` | POST | 查询蓝牙钥匙状态 | `vin` | BLE钥匙信息 |
| `car/charging/status` | POST | 查询充电状态 | `vin` | 充电状态数据 |

### 3.3 远程控制接口

#### 空调控制

| 接口路径 | 方法 | 功能描述 | 请求参数 |
|----------|------|----------|----------|
| `car/control/acc` | POST | 空调控制 | 见下方空调参数表 |

**空调控制参数**：

| 参数名 | 类型 | 说明 | 可选值 |
|--------|------|------|--------|
| `accOnOff` | String | 空调开关 | `"1"` (开), `"0"` (关) |
| `status` | String | 状态辅助 | `"1"` (开), `"0"` (关) |
| `temperature` | String | 目标温度 | `"16"` ~ `"32"` |
| `tmSetActTemp` | String | 设置温度（兼容） | 同 temperature |
| `blowerLvl` | String | 风速档位 | `"1"` ~ `"9"` |
| `duration` | String | 运行时长（分钟） | `"1"` ~ `"60"` |
| `mode` | String | 模式 | `"cool"`, `"heat"`, `"vent"`, `"auto"`, `"defog"` |
| `front` | String | 前挡除雾 | `"1"` (开), `"0"` (关) |
| `rear` | String | 后挡除雾 | `"1"` (开), `"0"` (关) |
| `vin` | String | 车辆识别码 | VIN码 |

#### 车门锁控制

| 接口路径 | 方法 | 功能描述 | 请求参数 |
|----------|------|----------|----------|
| `car/control/doorLock` | POST | 车门锁控制 | 见下方门锁参数表 |

**门锁控制参数**：

| 参数名 | 类型 | 说明 | 可选值 |
|--------|------|------|--------|
| `vin` | String | 车辆识别码 | VIN码 |
| `action` | String/Int | 控制动作 | `"LOCK"` / `"UNLOCK"` |
| | | | `"MANUAL_LOCK"` / `"MANUAL_UNLOCK"` |
| | | | `0` (解锁), `1` (锁车) |

#### 其他控制

| 接口路径 | 方法 | 功能描述 | 请求参数 |
|----------|------|----------|----------|
| `car/control/window` | POST | 车窗控制 | `vin`, `status` (0=关, 1=开) |
| `car/control/tailgate` | POST | 后备箱控制 | `vin`, `action` (`"toggle"`) |
| `car/control/searchCar` | POST | 寻车功能 | `vin` |
| `car/control/ignition/authorize` | POST | 远程启动授权 | `vin` |

### 3.4 充电相关接口

| 接口路径 | 方法 | 功能描述 | 请求参数 |
|----------|------|----------|----------|
| `car/control/charging` | POST | 充电控制 | `vin`, `action`, `chargeMode`, `chargeTime`(可选) |
| `car/option/smart/charge/query` | POST | 查询智能充电状态 | `vin` |
| `car/option/smart/charge/reserve` | POST | 预约智能充电 | `vin`, `chargeTime`, `chargeMode` |

---

## 四、车辆状态字段说明

### 4.1 空调状态字段

| 字段名 | 类型 | 说明 | 取值范围 |
|--------|------|------|----------|
| `acStatus` | Int | 空调状态 | 0=关闭, 1=开启 |
| `tmActTemp` | Int | 实际温度 | -40 ~ 60 ℃ |
| `tmSetActTemp` | Int | 设置温度 | 16 ~ 32 ℃ |
| `interiorTemperature` | Int | 车内温度 | -40 ~ 60 ℃ |
| `blowerLvl` / `acWindLevel` | Int | 风速档位 | 0=关闭, 1~9=档位 |
| `acActMode` / `accMode` | String | 空调模式 | cool/heat/vent/auto/defog |
| `frontDefog` | Int | 前挡除雾 | 0=关, 1=开 |
| `rearDefog` | Int | 后挡除雾 | 0=关, 1=开 |

### 4.2 车门状态字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `doorLockStatus` | Int | 车门锁状态 (0=解锁, 1=锁定) |
| `doorOpenStatus` | Int | 车门开启状态 (0=关闭, 1=开启) |
| `door1LockStatus` ~ `door4LockStatus` | Int | 四个车门分别的锁状态 |
| `door1OpenStatus` ~ `door4OpenStatus` | Int | 四个车门分别的开启状态 |

### 4.3 车辆基础状态

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `carOnline` | Boolean | 车辆是否在线 |
| `odometer` | Long | 总里程数 (km) |
| `range` | Int | 剩余续航里程 (km) |
| `battery` / `batteryLevel` | Int | 电池电量 (%) |
| `charging` | Boolean | 是否在充电 |
| `chargeStatus` | String | 充电状态 |

---

## 五、错误码说明

| 错误码 | 描述 | 解决方案 |
|--------|------|----------|
| `result=false` | 业务逻辑失败 | 查看 `errorMessage` 字段 |
| `notSuccess=true` | 操作未成功 | 查看具体错误信息 |
| 网络超时 | 网络连接问题 | 检查网络状态 |
| 401 Unauthorized | 凭证失效 | 重新登录获取新凭证 |
| 403 Forbidden | 无访问权限 | 检查 clientId/clientSecret |
| 500 Internal Error | 服务器错误 | 稍后重试 |

---

## 六、代码文件索引

| 文件路径 | 功能说明 |
|----------|----------|
| `api/ApiConfig.kt` | API 基础配置常量 |
| `api/WulingApiClient.kt` | API 请求客户端实现 |
| `utils/SignUtils.kt` | 签名工具类（SHA-256） |
| `utils/HttpClient.kt` | HTTP 客户端配置 |
| `model/LoginResponse.kt` | 登录响应数据模型 |
| `model/UserInfo.kt` | 用户信息数据模型 |
| `model/WulingCarInfo.kt` | 车辆信息数据模型 |
