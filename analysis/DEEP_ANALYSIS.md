# 50car APP(com.wuling.app)深度逆向分析报告

> 分析日期：2026-08-26 · 分析对象：官方加固壳脱壳 dump(classes.dex 9859 类 / classes2.dex 2230 类)
> 外层 7z 密码 `dump`；内层为伪装 .7z 扩展名的 ZIP;dex 头 checksum 损坏已在解析层容错

---

## 1. 应用真实身份

| 项 | 值 |
|---|---|
| 应用名 | 五菱第三方车控 APP("五菱车控"/50car) |
| 包名 | `com.wuling.app`(Application: `WulingApplication`,Hilt) |
| 开发者 | 独立开发者 "stargazerzzh"(菱粉)— Gitee: `gitee.com/stargazerzzh` |
| 自有后端 | **https://50car.vip**(Supabase:`/auth/v1/`、`/rest/v1/`)→ "50car" 项目名来源 |
| 后端端点 | `/login`、`/payment`(支付页)、`/rest/v1/qq_bindings`、`/api/leaderboard`(邀请榜)、`/api/delete-account` |
| 第三方登录 | QQ 互联(openmobile.qq.com OAuth2.0) |
| 商业模式 | **激活码付费**:年卡/永久赞助，推荐码返利(可提现)、一天体验卡；QQ 群 `qm.qq.com/q/QmD4pCVcUa` |
| 同源开源项目 | **菱粉工具箱** gitee.com/stargazerzzh/lingfanstools(**GPL-3.0**,22 commits，作者明确欢迎 AI 二次开发) |

内置协议自述：第三方非官方工具、账号密码/Token/激活码三种登录、"无广告无内购"(但激活码+返利提现构成实际商业化)。

## 2. 架构与技术栈

- **UI**:Jetpack Compose + **Material 3**(material3 546 类)— 原生 MD3 体系；含**液态玻璃(Liquid Glass)主题**(ThemePreferences.homeGlassTheme、模糊/透明度/背景图/自定义色板/小组件样式)
- **DI**:Dagger Hilt;**存储**:Room-less,DataStore Preferences(ThemePreferences/NotificationPreferences/AmapKeyManager/mqttCredentialStore 等)
- **网络**:OkHttp 4.9(裸 HttpLoggingInterceptor,no Retrofit)+ Gson;**推送/车联通道**:Eclipse Paho MQTT
- **加密**:Google Tink + com.flyfish233.crypto;请求签名 SHA-256 + MD5
- **车机直连**:`com.flyfishxu.kadb`(纯 Kotlin **ADB** 客户端,122 类)+ `org.lsposed.hiddenapibypass`
- **近场**:`androidx.window.area`、BLE(CompanionDevice 伴生绑定)、NFC(NDEF 写入 + HCE NfcCardService)
- **后台**:WorkManager(WidgetRefreshWorker)、前台服务 MqttKeepAliveService、WulingAccessibilityService(无障碍保活)、ShakeControlService

## 3. 四大能力域

### 3.1 云端车控(SGMW OpenAPI,`data/api/WulingAPI`)
- BaseURL:`https://openapi.baojun.net/junApi/sgmw`,伪装官方"菱菱邦"(channel=linglingbang, appCode=1661, version=V8.2.3.1)
- 请求头:`sgmwaccesstoken/timestamp/nonce/clientid/clientsecret/appcode/appversion/system/systemversion/signature`;密钥 `8c5a7a7d7d6f4a5e8c6b8d9e0f1a2b3c`(提取自官方 APK)
- 功能 API:`authorizeIgnition`(远程启动授权)、`checkCarStatus`、`controlAC`、`controlDoorLock`、`controlWindow`、`controlTailgate`、`searchCar`(鸣笛闪灯寻车)、`queryAllCar`、`switchFavoriteCar`、`queryChargeReserve`/`reserveCharge`(预约充电)、`queryTirePressure`(胎压)、`fetchYesterdayMileage`、`getBleKeyInfo`、`getMqttCredentials`、`sendCommand` 通用指令、`reverseGeocode`(高德逆地理)
- 工程细节:`executeWithRetry`(重试)、内存缓存 `cacheExpiryTime=60s`、`TokenInvalidCallback` 失效回调、`UpdateManager` 自检更新+版本历史

### 3.2 MQTT 车联(`data/mqtt`)
- Broker:**`tcp://parkingdata.sgmwcloud.com.cn:1883`(明文 TCP,无 TLS)**
- Topics:`{vin}/prod/sgmw/vehicle/{status|control|car_check_authorize|parking}/business`,QoS 1;clientId=`{vin}_{手机号尾}`;凭据走 `getMqttCredentials` 动态获取,`MqttKeepAliveService` 常驻保活,`scheduleReconnect` 自动重连

### 3.3 车机 ADB 工具箱(kadb,与开源"菱粉工具箱"同源)
- 连接车机 ADB:截图、投屏、录屏、重启、无线 ADB 保活
- 应用管理：可启动应用列表、图标提取、停用/关闭应用、已卸载残留清理
- 文件管理：共享存储概览、大文件/内存占用、上传/下载到本机
- 车机美化：图标素材包("命中当前车机")、自定义图标上传裁剪

### 3.4 近场钥匙与交互
- **BLE 数字钥匙**:`getBleKeyInfo` + `BleQuickControlReceiver` + CompanionDevice **伴生绑定**(唤醒直连/回退扫描/重试冷却,日志大量"伴生设备…"状态机)
- **NFC**:MainActivity 直写 NDEF 卡(`application/vnd.com.wuling.app.nfc`,负载 `action:xxx;vin:xxx;temperature:xx`)→ 实体 NFC 卡控车;`NfcCardService`(HCE)/`NfcHandlerActivity`
- **摇晃控车**:阈值/次数/冷却可配,摇晃解锁可联动自动开空调(`shakeUnlockAutoAc`)
- **桌面小组件**:2×2 快捷控车 + 4×4 状态(锁车/解锁/空调/启动/寻车/窗开关各自 Provider)
- 提醒体系：电量/油量/胎压/充电完成/12V 低压电池亏电/锁车提醒/车窗提醒(阈值均可配)

## 4. 安全与合规发现(隐患清单)

1. **`WulingLoginHelper.createTrustAllClient()`**:登录链路信任全部证书 → 账号密码可被中间人劫持(最高危)
2. **MQTT 明文 1883**:车控指令无 TLS,局域网可嗅探/重放
3. **硬编码机密**:SGMW 官方签名密钥、高德 WebApi key(`AmapKeyManager`)、和风天气 id 均打包在 dex
4. 用户协议明文禁止破解激活/二改分发;"永久赞助/年卡+返利提现"为商业授权体系
5. GPS/位置经 `m.amap.com`/`uri.amap.com/navigation` 拉起导航,`locate.ms.tn`(PATEO)上报

## 5. 与前一次任务(优化逻辑 + 谷歌 MD 适配)的关系

**现状已高度 MD3 化**(Compose Material3 + 液态玻璃 + 全量自定义主题),粗糙点集中在交互逻辑。可安全改进项(不触碰授权体系)：

### 操作逻辑优化点(按发现排序)
1. **控车指令闭环**:命令下发后靠 MQTT status topic 回执 → 应给每条指令做"发送→回执→超时回滚"状态机 UI(current:executeWithRetry 只重试 HTTP,无指令级 pending 态)
2. **防误触分级**:解锁/后备箱/远程启动(authorizeIgnition)应二次确认 + 安全提示(现有文案只在协议里);摇晃/小组件/便捷入口需一致的确认策略
3. **连接可视化**:MQTT 连接态(MqttConnectionState 已建模)应显示在首页;断线自动重连已有,但缺用户可见的退避提示
4. **缓存策略**:`cache=60s` 过短且仅内存缓存 → 状态页应持久化上次快照 + 显示"数据时间戳"(高德云图那种"最后更新于")
5. **登录态**:`TokenInvalidCallback` 应跳登录页并保留现场(深链回到原页面)
6. **保活引导**:无障碍/后台服务为存活前提 → 首次启动应做电池优化白名单、自启动引导(车厂 ROM 尤其需要)
7. **小组件一致性**:NFC/摇晃/小组件/快捷方式为平行入口,动作定义应收敛到同一 `ControlCommand` 模型(代码里已存在 `data/model/ControlCommand`,UI 层未见统一)

### MD3 适配优化点
- 液态玻璃为自定义体系 → 建议叠加在 MD3 `ColorScheme` 之上而非替换,保证 Material You 动态取色(android 12+)可用
- 控车按钮网格 → `ElevatedCard` + 状态色语义(error=解锁/警示, tertiary=空调, primary=常规)
- 胎压四项 → `ListItem` + `AssistChip` 异常标红;充电页 → `LinearProgressIndicator`(MD3 波浪动画版)
- 预测性返回手势 + 大屏自适应(车机/平板横屏)`NavigationSuiteScaffold`
- 字体缩放、高对比度、触摸目标 ≥48dp(a11y)

## 6. 合法二次开发路径(重要)

作者已在 Gitee 开源同生态项目 **菱粉工具箱(GPL-3.0)**:`gitee.com/stargazerzzh/lingfanstools`,README 明确"二次开发前配置好签名文件…让 AI 看一眼项目结构就知道怎么做"。
- 想改界面/逻辑:**fork 该仓库在源码层开发**(GPL-3.0 义务:衍生作品继续开源),远优于 smali 级修改
- 本 dump 仅建议用于研读协议/思路,不建议重回打包分发(违反其 EULA,且有法律风险)

## 7. 产物清单(仓库 analysis/)

| 文件 | 内容 |
|---|---|
| `FINDINGS.md` | 初步发现(身份、激活机制结论) |
| `DEEP_ANALYSIS.md` | 本报告 |
| `dex_inventory.txt` | 两个 dex 的包分布 + 全部类名 |
| `class_tree.txt` | com.wuling.app 包结构树(132 混淆类 + 具名类) |
| `class_strings.txt` | 逐类字符串常量映射 |
| `all_strings.txt` | 92328 条字符串全集 |
| `zh_ui_labels.txt` | 1349 条中文 UI 短标签 |
| `decompiled_entries.txt` | MainActivity/Application/WulingAPI/MQTT 等 10 个关键类反编译 |
| `dex_load.py` | 可复用的容错 DEX 加载器(跳过损坏注解) |
