# 车辆状态获取失败问题修复文档

## 问题概述
WulingCar-v2.1.0-debug.apk 无法获取车辆状态，通过反编译对比分析发现签名算法、设备参数和请求头与服务器期望的不匹配。

## 分析过程

### 1. 反编译 APK
使用 Android SDK 自带的 `dexdump` 工具反编译 APK 中的 DEX 文件：

```bash
# 提取 DEX 文件
unzip -o WulingCar-v2.1.0-debug.apk "*.dex" -d apk_analysis/

# 分析 WulingApiClient 类 (位于 classes6.dex)
/opt/android-sdk/build-tools/35.0.0/dexdump -d classes6.dex | grep -A 300 "Class descriptor.*WulingApiClient;"

# 提取关键字符串
strings classes*.dex | grep -iE '(openapi|baojun|junApi|sgmw|wuling|car/control|car/status|sign|secret|md5|sha256)'
```

### 2. 提取的 API 关键信息

| 项目 | 值 |
|------|------|
| 基础 URL | `https://openapi.baojun.net/junApi/sgmw/` |
| 登录 API | `user/login` (POST) |
| 车辆状态 API | `car/check/all` (POST) |
| appCode | `sgmw_llb` |
| appVersion | `1861` |
| system | `android` |
| systemVersion | `16` |

## 发现的问题

### 问题 1: 签名算法错误（核心原因）

**APK 中的签名算法 (正确):**
```
算法: MD5
签名数据: accessToken + timestamp + nonce + clientId + clientSecret + "sgmw_llb1861android16" + "android"
```

**我们项目中的签名算法 (错误):**
```
算法: SHA-256
签名数据: accessToken + timestamp + nonce + clientId + clientSecret + appCode + appVersion + system + systemVersion
```

**修复:** 将签名算法从 SHA-256 改为 MD5，签名数据格式改为固定字符串拼接。

### 问题 2: 设备参数不匹配

| 参数 | APK 中的值 (正确) | 我们之前的值 (错误) |
|------|-------------------|-------------------|
| appVersionCode | `1861` | `1691` |
| version | `V8.2.23` | `V8.2.17` |
| systemVersion | `16` | `10` |
| deviceModel | `RMX6699` | `MI 8` |
| deviceBrand | `realme` | `Xiaomi` |
| IMEI | `a-a-a-bc4bf296b62bb0a9` | `a-c62b2f538bf34758` |

### 问题 3: 缺少请求头

APK 发送 23 个请求头，比我们的项目多一个 `sgmwplatformno` 头，且没有 `imsi` 头。

## 修复内容

### 文件 1: `app/src/main/java/com/example/vehicleinfo/utils/SignUtils.kt`

```diff
- 使用 SHA-256 算法
- 签名数据: accessToken + timestamp + nonce + clientId + clientSecret + appCode + appVersion + system + systemVersion
+ 使用 MD5 算法
+ 签名数据: accessToken + timestamp + nonce + clientId + clientSecret + "sgmw_llb1861android16" + "android"
```

### 文件 2: `app/src/main/java/com/example/vehicleinfo/api/ApiConfig.kt`

```diff
- SGMW_APPVERSION = "1691"
- SGMW_SYSTEMVERSION = "10"
- APP_VERSION = "V8.2.17"
- DEVICE_MODEL = "MI 8"
- DEVICE_BRAND = "Xiaomi"
- IMEI = "a-c62b2f538bf34758"
+ SGMW_APPVERSION = "1861"
+ SGMW_SYSTEMVERSION = "16"
+ APP_VERSION = "V8.2.23"
+ DEVICE_MODEL = "RMX6699"
+ DEVICE_BRAND = "realme"
+ IMEI = "a-a-a-bc4bf296b62bb0a9"
```

### 文件 3: `app/src/main/java/com/example/vehicleinfo/api/WulingApiClient.kt`

```diff
- generateSign(accessToken, timestamp, nonce, clientId, clientSecret, appCode, appVersion, system, systemVersion)
- 请求头包含 "imsi" 字段
- 缺少 "sgmwplatformno" 字段
+ generateSign(accessToken, timestamp, nonce, clientId, clientSecret)
+ 移除 "imsi" 请求头
+ 添加 "sgmwplatformno" 请求头 (值为 "android")
```

## 完整请求头列表（修复后共 23 个）

| 序号 | Header 名 | 值 |
|------|-----------|-----|
| 0 | Accept | application/json |
| 1 | Content-Type | application/json; charset=UTF-8 |
| 2 | User-Agent | okhttp/4.9.0 |
| 3 | channel | linglingbang |
| 4 | platformNo | Android |
| 5 | appVersionCode | 1861 |
| 6 | version | V8.2.23 |
| 7 | imei | a-a-a-bc4bf296b62bb0a9 |
| 8 | deviceModel | RMX6699 |
| 9 | deviceBrand | realme |
| 10 | deviceType | Android |
| 11 | accessChannel | 1 |
| 12 | sgmwaccesstoken | (动态值) |
| 13 | sgmwtimestamp | (动态值) |
| 14 | sgmwnonce | (动态值) |
| 15 | sgmwclientid | (动态值) |
| 16 | sgmwclientsecret | (动态值) |
| 17 | sgmwappcode | sgmw_llb |
| 18 | sgmwappversion | 1861 |
| 19 | sgmwsystem | android |
| 20 | sgmwsystemversion | 16 |
| 21 | sgmwsignature | (MD5 签名值) |
| 22 | sgmwplatformno | android |

## 签名算法详细说明

```
签名数据 = accessToken + timestamp + nonce + clientId + clientSecret + "sgmw_llb1861android16" + "android"
签名 = MD5(签名数据).lowercase()
```

其中 `"sgmw_llb1861android16"` 是固定密钥，由以下部分拼接而成：
- `sgmw_llb` (appCode)
- `1861` (appVersion)
- `android` (system)
- `16` (systemVersion)

## 构建版本

| 版本 | 说明 |
|------|------|
| v5.0.1 (build 37) | 修复前版本 |
| v5.0.2 (build 38) | 修复后版本 |

## 下载链接

- [litterbox (72小时有效)](https://litter.catbox.moe/3cuoun.apk)
- [gofile.io](https://gofile.io/d/8HUCt1)