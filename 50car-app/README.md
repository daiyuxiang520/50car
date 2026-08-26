# 50Car 车控(社区版)

参照 50car(com.wuling.app)逆向分析结论**从零原创**的五菱系新能源车控 APP 工程。
Kotlin + Jetpack Compose + Material 3,targetSdk 36(Android 16)。

> ⚠️ 本工程是**全新原创代码**,不包含原 APP 的任何代码、资源、私有密钥或签名。
> 车控能力通过 `CarApi` 接口抽象,默认绑定离线 Mock 实现;接入真实车辆请实现
> 你自己的授权接入层(见 `di/AppModule.kt`)。请勿填入从官方 APP 提取的密钥。

## 功能(对应原 50car 能力域)

| 域 | 本工程 | 说明 |
|---|---|---|
| 车辆状态 | ✅ 首页 | 电量/续航/里程/温度/胎压/车门车窗/充电,**带数据时间戳** |
| 远程控车 | ✅ 控制页 | 锁车/解锁/后备箱/车窗/空调/远程启动/寻车,**风险分级二次确认** |
| 指令闭环 | ✅ | 发送→回执→**25s 超时回滚**(原 APP 无指令级状态机) |
| 充电管理 | ✅ | 启停充电 + 预约充电(低谷时段) |
| MQTT 车联 | ✅ `MqttManager` | QoS1、自动重连、指数退避、连接态用户可见 |
| 保活 | ✅ | FGS(dataSync)+ BOOT 恢复 + **电池白名单引导**(原 APP 从不引导) |
| NFC 控车卡 | ✅ | 解析 `action:xxx;vin:xxx;temperature:xx` NDEF 负载直发指令 |
| 小组件 | ✅ 2x2 | 快捷锁车/解锁(Glance/RemoteViews,执行层留扩展点) |
| 主题 | ✅ | **纯 MD3 + Material You 动态取色**(API31+) |
| 大屏适配 | ✅ | NavigationSuiteScaffold(竖屏底栏/横屏 Rail) |

## 编译

需要:Android Studio Narwhal+(或 JDK 17 + Android SDK 36)

```bash
# 首次生成 wrapper 后
gradle wrapper
./gradlew assembleDebug        # 产出 app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # 装到连接的设备(需 ADB)
```

演示模式:登录页输入任意账号 + ≥6 位密码即可进入(Mock 数据)。

## Android 16 (API 36) 适配核对表

| 新规 | 处理 |
|---|---|
| edge-to-edge 强制开启(target36 不可关闭) | `enableEdgeToEdge()` + 各页 Scaffold/safeDrawingPadding 处理 insets |
| 预测性返回手势默认启用 | manifest 显式 `enableOnBackInvokedCallback=true`;返回栈无明显动画断点 |
| FGS 类型强制声明 | `KeepAliveService` 声明 `dataSync` 类型 + API34 运行时传类型 |
| 通知运行时权限(13+/16) | 我的→通知权限引导跳系统设置页 |
| 后台启动限制 / Doze | WorkManager 兜底 + 电池优化白名单引导 + BootReceiver 恢复 |
| 16KB 页对齐(原生库) | 全部依赖为纯 Java/Kotlin(Paho 纯 Java),无 .so,天然兼容 |
| 明文流量默认禁止 | manifest `usesCleartextTraffic=true`(仅为 tcp:// MQTT;生产建议 ssl://) |
| 备份/设备迁移规则 | `backup_rules.xml`/`data_extraction_rules.xml`,token 文件排除云备份 |

## 结构

```
app/src/main/java/com/fiftycar/app/
├── FiftyCarApp.kt          # Hilt 入口,通知渠道
├── MainActivity.kt         # edge-to-edge,NFC NDEF 解析
├── data/                   # 模型/CarApi 抽象/Mock/仓库(指令状态机)/TokenStore
├── mqtt/MqttManager.kt    # Paho 封装,退避重连
├── service/                # KeepAliveService(dataSync FGS)+ BootReceiver
├── widget/                 # 2x2 小组件 + 指令广播
├── di/AppModule.kt         # CarApi 绑定(换绑即接真实接口)
└── ui/                     # theme(MD3 动态取色)/screens(5 页)/components/vm
```

## 与原 APP 的合规边界(务必阅读)

1. 原 50car 用户协议禁止逆向/二改/分发其安装包;**本工程不触碰其安装包**,是独立源码。
2. `8c5a7a…` 等原分析中出现的签名密钥为其官方协议实现细节,**本工程一律不收录**。
3. 若要真实控车:使用你自己车辆的账号 Token(用户自行登录获得),或接入自有后端;
   由此产生的账号风险由使用者自负(参考原 APP 协议风险提示)。
