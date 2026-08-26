# 五菱 PATEO API 技术文档

## 一、API 基础配置

### 1.1 基础地址

```
API_BASE_URL = https://openapi.baojun.net/junApi/sgmw
```

### 1.2 应用参数

| 参数名 | 值 | 说明 |
|--------|-----|------|
| APP_CODE | `sgmw_llb` | 应用标识 |
| APP_VERSION | `1691` | 应用版本号 |
| SYSTEM | `android` | 操作系统类型 |
| SYSTEM_VERSION | `10` | 系统版本 |
| CHANNEL | `linglingbang` | 渠道标识 |
| PLATFORM_NO | `Android` | 平台标识 |
| ACCESS_CHANNEL | `1` | 接入渠道 |
| APP_VERSION_NAME | `V8.2.17` | 应用版本名称 |

### 1.3 设备参数

| 参数名 | 默认值 | 说明 |
|--------|--------|------|
| IMEI | `a-c62b2f538bf34758` | 设备唯一标识 |
| DEVICE_MODEL | `MI 8` | 设备型号 |
| DEVICE_BRAND | `Xiaomi` | 设备品牌 |
| DEVICE_TYPE | `Android` | 设备类型 |
| USER_AGENT | `okhttp/4.9.0` | HTTP 客户端 |

---

## 二、签名机制（SHA-256）

### 2.1 签名算法

- **算法类型**：SHA-256
- **输出格式**：32 位十六进制字符串（小写）
- **编码方式**：UTF-8

### 2.2 签名拼接规则

```
signStr = accessToken 
        + timestamp 
        + nonce 
        + clientId 
        + clientSecret 
        + appCode 
        + appVersion 
        + system 
        + systemVersion
```

### 2.3 签名生成步骤

```python
# Python 实现示例
import hashlib

def generate_signature(
    access_token: str,      # 登录获取的 token
    timestamp: str,         # 当前时间戳（毫秒）
    nonce: str,             # 随机字符串（10位字母）
    client_id: str,         # 登录获取的 clientId
    client_secret: str,     # 登录获取的 clientSecret
    app_code: str = "sgmw_llb",
    app_version: str = "1691",
    system: str = "android",
    system_version: str = "10"
) -> str:
    """
    生成 API 请求签名
    
    Returns:
        SHA-256 签名（小写十六进制字符串）
    """
    sign_str = (
        access_token +
        timestamp +
        nonce +
        client_id +
        client_secret +
        app_code +
        app_version +
        system +
        system_version
    )
    signature = hashlib.sha256(sign_str.encode('utf-8')).hexdigest().lower()
    return signature
```

### 2.4 Kotlin 实现（Android）

```kotlin
object SignUtils {
    
    fun generateTimestamp(): String {
        return System.currentTimeMillis().toString()
    }
    
    fun generateNonce(): String {
        val chars = ('a'..'z') + ('A'..'Z')
        return (1..10).map { chars[Random.nextInt(chars.size)] }.joinToString("")
    }
    
    fun generateSign(
        accessToken: String,
        timestamp: String,
        nonce: String,
        clientId: String,
        clientSecret: String,
        appCode: String,
        appVersion: String,
        system: String,
        systemVersion: String
    ): String {
        val signStr = accessToken +
            timestamp +
            nonce +
            clientId +
            clientSecret +
            appCode +
            appVersion +
            system +
            systemVersion
        return sha256(signStr).lowercase()
    }
    
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
```

### 2.5 请求头字段

每个 API 请求必须包含以下签名相关字段：

| Header 字段 | 说明 |
|-------------|------|
| `sgmwaccesstoken` | 登录获取的 accessToken |
| `sgmwtimestamp` | 当前时间戳（毫秒） |
| `sgmwnonce` | 随机字符串 |
| `sgmwclientid` | 登录获取的 clientId |
| `sgmwclientsecret` | 登录获取的 clientSecret |
| `sgmwappcode` | 应用标识 |
| `sgmwappversion` | 应用版本号 |
| `sgmwsystem` | 操作系统类型 |
| `sgmwsystemversion` | 系统版本 |
| `sgmwsignature` | 生成的 SHA-256 签名 |

---

## 三、AES 加密算法

### 3.1 算法参数

| 参数 | AES-128 | AES-256 |
|------|---------|---------|
| **算法** | AES-128-CBC | AES-256-CBC |
| **密钥长度** | 16 字节 | 32 字节 |
| **块大小** | 16 字节 | 16 字节 |
| **填充方式** | PKCS7 | PKCS7 |
| **IV 向量** | 全零 16 字节 | 全零 16 字节 |
| **输出编码** | Base64 | Base64 |

### 3.2 密钥列表

#### 3.2.1 libencrypt.so 密钥（用于 CheckCode 加解密）

| 密钥名 | Hex 值 | 用途 |
|--------|--------|------|
| Key1 | `6cf70fabc636f5e6569cd03e194e5769` | CheckCode 主密钥 |
| Key2 | `6cf287828b840086b704f680e70cd2d7` | CheckCode 备用密钥 |
| Key3 | `af19d5b9c8e3db4baa47af8d75aa6adb` | CheckCode 备用密钥 |

#### 3.2.2 libdexvmp*.so 密钥（用于 Dex 保护）

| 密钥名 | Hex 值 | 用途 |
|--------|--------|------|
| VMP1 | `28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93` | Dex 解密 |
| VMP2 | `32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7` | Dex 解密 |
| VMP3 | `BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0` | Dex 解密 |

#### 3.2.3 libSecretKey.so 密钥（海康威视 SDK）

| 密钥名 | Hex 值 | 用途 |
|--------|--------|------|
| Secret | `5bfd505c8e4e90c379bb66aa946ea015` | 海康威视 SDK |

### 3.3 AES-128 加密实现

```python
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
import base64

# 密钥定义
KEY_ENCRYPT_1 = bytes.fromhex('6cf70fabc636f5e6569cd03e194e5769')
KEY_ENCRYPT_2 = bytes.fromhex('6cf287828b840086b704f680e70cd2d7')
KEY_ENCRYPT_3 = bytes.fromhex('af19d5b9c8e3db4baa47af8d75aa6adb')

# 默认 IV
DEFAULT_IV = b'\x00' * 16

def aes128_encrypt(plaintext: str, key: bytes = None, iv: bytes = None) -> str:
    """
    AES-128-CBC 加密
    
    Args:
        plaintext: 待加密的明文字符串
        key: 16 字节密钥，默认使用 KEY_ENCRYPT_1
        iv: 16 字节 IV，默认全零
    
    Returns:
        Base64 编码的密文
    """
    if key is None:
        key = KEY_ENCRYPT_1
    if iv is None:
        iv = DEFAULT_IV
    
    plaintext_bytes = plaintext.encode('utf-8')
    cipher = AES.new(key, AES.MODE_CBC, iv=iv)
    padded = pad(plaintext_bytes, 16)
    encrypted = cipher.encrypt(padded)
    
    return base64.b64encode(encrypted).decode('utf-8')
```

### 3.4 AES-256 加密实现

```python
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad
import base64

# 密钥定义
KEY_VMP_1 = bytes.fromhex('28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93')
KEY_VMP_2 = bytes.fromhex('32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7')
KEY_VMP_3 = bytes.fromhex('BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0')

def aes256_encrypt(plaintext: str, key: bytes = None, iv: bytes = None) -> str:
    """
    AES-256-CBC 加密
    
    Args:
        plaintext: 待加密的明文字符串
        key: 32 字节密钥，默认使用 KEY_VMP_1
        iv: 16 字节 IV，默认全零
    
    Returns:
        Base64 编码的密文
    """
    if key is None:
        key = KEY_VMP_1
    if iv is None:
        iv = DEFAULT_IV
    
    plaintext_bytes = plaintext.encode('utf-8')
    cipher = AES.new(key, AES.MODE_CBC, iv=iv)
    padded = pad(plaintext_bytes, 16)
    encrypted = cipher.encrypt(padded)
    
    return base64.b64encode(encrypted).decode('utf-8')
```

### 3.5 CheckCode 加密

```python
def encrypt_checkcode(plaintext: str, key_index: int = 1) -> str:
    """
    加密生成 CheckCode
    
    Args:
        plaintext: 待加密的明文字符串
        key_index: 密钥索引 (1, 2, 3)
    
    Returns:
        Base64 编码的 CheckCode
    """
    keys = [KEY_ENCRYPT_1, KEY_ENCRYPT_2, KEY_ENCRYPT_3]
    if key_index < 1 or key_index > 3:
        raise ValueError("key_index 必须是 1, 2 或 3")
    
    key = keys[key_index - 1]
    return aes128_encrypt(plaintext, key)
```

---

## 四、AES 解密算法

### 4.1 AES-128 解密实现

```python
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad
import base64

def aes128_decrypt(ciphertext_b64: str, key: bytes = None, iv: bytes = None) -> str:
    """
    AES-128-CBC 解密
    
    Args:
        ciphertext_b64: Base64 编码的密文
        key: 16 字节密钥，默认使用 KEY_ENCRYPT_1
        iv: 16 字节 IV，默认全零
    
    Returns:
        解密后的明文字符串
    
    Raises:
        ValueError: 解密失败或密钥不正确
    """
    if key is None:
        key = KEY_ENCRYPT_1
    if iv is None:
        iv = DEFAULT_IV
    
    # Base64 解码
    ciphertext = base64.b64decode(ciphertext_b64)
    
    # AES-CBC 解密
    cipher = AES.new(key, AES.MODE_CBC, iv=iv)
    decrypted = cipher.decrypt(ciphertext)
    
    # 去除 PKCS7 填充
    plaintext = unpad(decrypted, 16)
    
    return plaintext.decode('utf-8')
```

### 4.2 AES-256 解密实现

```python
def aes256_decrypt(ciphertext_b64: str, key: bytes = None, iv: bytes = None) -> str:
    """
    AES-256-CBC 解密
    
    Args:
        ciphertext_b64: Base64 编码的密文
        key: 32 字节密钥，默认使用 KEY_VMP_1
        iv: 16 字节 IV，默认全零
    
    Returns:
        解密后的明文字符串
    """
    if key is None:
        key = KEY_VMP_1
    if iv is None:
        iv = DEFAULT_IV
    
    ciphertext = base64.b64decode(ciphertext_b64)
    cipher = AES.new(key, AES.MODE_CBC, iv=iv)
    decrypted = cipher.decrypt(ciphertext)
    plaintext = unpad(decrypted, 16)
    
    return plaintext.decode('utf-8')
```

### 4.3 CheckCode 解密

```python
def decrypt_checkcode(checkcode: str, key_index: int = 1) -> str:
    """
    解密 CheckCode
    
    Args:
        checkcode: Base64 编码的加密字符串
        key_index: 密钥索引 (1, 2, 3)
    
    Returns:
        解密后的明文字符串
    """
    keys = [KEY_ENCRYPT_1, KEY_ENCRYPT_2, KEY_ENCRYPT_3]
    if key_index < 1 or key_index > 3:
        raise ValueError("key_index 必须是 1, 2 或 3")
    
    key = keys[key_index - 1]
    return aes128_decrypt(checkcode, key)
```

### 4.4 批量密钥尝试解密

```python
def try_decrypt_all_keys(checkcode: str) -> dict:
    """
    尝试用所有密钥解密
    
    Args:
        checkcode: Base64 编码的密文
    
    Returns:
        {key_index: plaintext_or_error}
    """
    results = {}
    for i in range(1, 4):
        try:
            plaintext = decrypt_checkcode(checkcode, key_index=i)
            results[f"Key{i}"] = plaintext
        except Exception as e:
            results[f"Key{i}"] = f"Error: {str(e)}"
    return results
```

### 4.5 解密流程示例

```python
# 假设有一段 Base64 密文
ciphertext = "Vw8e5n2Lx4Rb7kQm..."

# 尝试所有密钥解密
results = try_decrypt_all_keys(ciphertext)
# 返回:
# {
#   "Key1": "user_id=12345&action=login",  # ✅ Key1 解密成功
#   "Key2": "Error: Padding is invalid",   # ❌ Key2 失败
#   "Key3": "Error: Padding is invalid"   # ❌ Key3 失败
# }
```

---

## 五、API 接口列表

### 5.1 认证相关

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 账号登录 | POST | `/user/login` | 手机号+密码登录 |
| 获取用户信息 | POST | `/user/info` | 获取当前用户信息 |

### 5.2 车辆管理

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询车辆列表 | POST | `/userCarRelation/queryCarList` | 获取用户绑定的车辆列表 |
| 查询默认车辆状态 | POST | `/userCarRelation/queryDefaultCarStatus` | 查询默认车辆状态 |
| 查询车辆状态 | POST | `/car/check/all` | 查询车辆整体状态 |
| 搜索车辆 | POST | `/car/control/searchCar` | 搜索指定 VIN 的车辆 |

### 5.3 远程控制

| 接口 | 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|------|
| 车窗控制 | POST | `/car/control/window` | vin, status | 控制车窗升降 |
| 车门锁控制 | POST | `/car/control/doorLock` | vin, action(LOCK/UNLOCK) | 锁车/解锁 |
| 空调控制 | POST | `/car/control/acc` | vin, mode, temperature | 控制空调 |
| 后备厢控制 | POST | `/car/control/tailgate` | vin, action | 控制后备厢 |
| 点火授权 | POST | `/car/control/ignition/authorize` | vin | 授权点火 |

### 5.4 充电相关

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询胎压 | POST | `/car/info/tire/pressure` | 查询实时胎压 |
| 查询昨日里程 | POST | `/car/yesterday/mileage` | 查询昨日行驶里程 |
| 查询 BLE 钥匙 | POST | `/car/control/ble/key/query` | 查询蓝牙钥匙状态 |
| 查询智能充电 | POST | `/car/option/smart/charge/query` | 查询智能充电状态 |
| 预约智能充电 | POST | `/car/option/smart/charge/reserve` | 预约充电时间 |
| 查询充电状态 | POST | `/car/charging/status` | 查询充电进行状态 |
| 充电控制 | POST | `/car/control/charging` | 控制充电启动/停止 |

---

## 六、完整请求示例

### 6.1 Python 示例

```python
import hashlib
import time
import random
import string
import requests
import json

class WulingClient:
    API_BASE = "https://openapi.baojun.net/junApi/sgmw"
    APP_CODE = "sgmw_llb"
    APP_VERSION = "1691"
    SYSTEM = "android"
    SYSTEM_VERSION = "10"
    
    def __init__(self):
        self.access_token = ""
        self.client_id = ""
        self.client_secret = ""
    
    def login(self, phone: str, password: str):
        """账号登录"""
        url = f"{self.API_BASE}/user/login"
        data = {
            "phone": phone,
            "password": password,
            "appCode": self.APP_CODE,
            "appVersion": self.APP_VERSION,
            "system": self.SYSTEM,
            "systemVersion": self.SYSTEM_VERSION
        }
        resp = requests.post(url, json=data)
        result = resp.json()
        
        if result.get("result"):
            self.access_token = result["data"]["accessToken"]
            self.client_id = result["data"]["clientId"]
            self.client_secret = result["data"]["clientSecret"]
            return True
        return False
    
    def _generate_signature(self, timestamp: str, nonce: str) -> str:
        """生成签名"""
        sign_str = (
            self.access_token +
            timestamp +
            nonce +
            self.client_id +
            self.client_secret +
            self.APP_CODE +
            self.APP_VERSION +
            self.SYSTEM +
            self.SYSTEM_VERSION
        )
        return hashlib.sha256(sign_str.encode()).hexdigest().lower()
    
    def _build_headers(self) -> dict:
        """构建请求头"""
        timestamp = str(int(time.time() * 1000))
        nonce = ''.join(random.choices(string.ascii_letters, k=10))
        signature = self._generate_signature(timestamp, nonce)
        
        return {
            "Accept": "application/json",
            "Content-Type": "application/json; charset=UTF-8",
            "User-Agent": "okhttp/4.9.0",
            "channel": "linglingbang",
            "platformNo": "Android",
            "appVersionCode": self.APP_VERSION,
            "version": "V8.2.17",
            "imei": "a-c62b2f538bf34758",
            "deviceModel": "MI 8",
            "deviceBrand": "Xiaomi",
            "deviceType": "Android",
            "accessChannel": "1",
            "sgmwaccesstoken": self.access_token,
            "sgmwtimestamp": timestamp,
            "sgmwnonce": nonce,
            "sgmwclientid": self.client_id,
            "sgmwclientsecret": self.client_secret,
            "sgmwappcode": self.APP_CODE,
            "sgmwappversion": self.APP_VERSION,
            "sgmwsystem": self.SYSTEM,
            "sgmwsystemversion": self.SYSTEM_VERSION,
            "sgmwsignature": signature
        }
    
    def query_car_status(self, vin: str) -> dict:
        """查询车辆状态"""
        url = f"{self.API_BASE}/car/check/all"
        headers = self._build_headers()
        resp = requests.post(url, json={"vin": vin}, headers=headers)
        return resp.json()
    
    def unlock_car(self, vin: str) -> dict:
        """解锁车辆"""
        url = f"{self.API_BASE}/car/control/doorLock"
        headers = self._build_headers()
        resp = requests.post(url, json={"vin": vin, "action": "UNLOCK"}, headers=headers)
        return resp.json()
```

### 6.2 签名生成示例

```python
# 示例：生成一个 API 请求签名
client = WulingClient()
client.access_token = "eyJhbGciOiJIUzI1NiJ9..."
client.client_id = "user_12345"
client.client_secret = "secret_abcde"

timestamp = "1700000000000"
nonce = "AbcDefGhiJ"

signature = client._generate_signature(timestamp, nonce)
print(f"签名: {signature}")
# 输出: 签名: a1b2c3d4e5f6...64位十六进制字符串
```

---

## 七、错误码说明

| 错误码 | 名称 | 原因 | 解决方案 |
|--------|------|------|----------|
| 50009 | SgmwAccessToken参数不合法 | Token 为空/格式错误/过期 | 重新登录获取 Token |
| 50010 | 签名验证失败 | 签名生成错误 | 检查签名拼接顺序 |
| 50011 | 参数缺失 | 必要参数未传 | 检查请求参数完整性 |
| 50012 | 业务逻辑错误 | 业务校验失败 | 检查请求条件 |

---

## 八、密钥提取来源

### 8.1 APK 结构

```
target.apk (ZIP 格式)
├── lib/
│   └── arm64-v8a/
│       ├── libencrypt.so          # 主加密库 (AES-128)
│       ├── libdexvmp.so           # Bangcle 保护壳 (AES-256)
│       ├── libdexvmp1.so          # Bangcle 保护壳 (AES-256)
│       ├── libdexvmp2.so          # Bangcle 保护壳 (AES-256)
│       ├── libSecretKey.so        # 海康威视 SDK 密钥
│       ├── libpateowb_crypto_tool.so  # 白盒加密
│       └── libwbsk_crypto_tool.so    # 白盒加密
├── classes.dex                    # 应用代码
└── AndroidManifest.xml             # 清单文件
```

### 8.2 密钥提取方法

```bash
# 1. 解压 APK
unzip target.apk -d extracted/

# 2. 搜索 so 库中的密钥
# 方法1: strings 搜索 hex 字符串
strings -n 16 libencrypt.so | grep -E '^[0-9a-fA-F]{16}$'

# 方法2: hexdump 查看 .data 段
hexdump -C libencrypt.so | grep -A5 "6cf70fab"

# 方法3: 使用 xxd
xxd -l 1024 -s <offset> libencrypt.so
```

### 8.3 密钥验证

```python
from Crypto.Cipher import AES
from Crypto.Util.Padding import pad, unpad
import base64

def verify_key(key_hex: str, plaintext: str = "test") -> bool:
    """验证密钥是否正确"""
    key = bytes.fromhex(key_hex)
    iv = b'\x00' * 16
    
    try:
        # 加密
        cipher = AES.new(key, AES.MODE_CBC, iv=iv)
        encrypted = cipher.encrypt(pad(plaintext.encode(), 16))
        
        # 解密
        cipher2 = AES.new(key, AES.MODE_CBC, iv=iv)
        decrypted = unpad(cipher2.decrypt(encrypted), 16)
        
        return decrypted.decode() == plaintext
    except:
        return False

# 验证所有密钥
keys = [
    '6cf70fabc636f5e6569cd03e194e5769',
    '6cf287828b840086b704f680e70cd2d7',
    'af19d5b9c8e3db4baa47af8d75aa6adb'
]

for key in keys:
    if verify_key(key):
        print(f"✅ {key[:16]}... 验证通过")
    else:
        print(f"❌ {key[:16]}... 验证失败")
```

---

## 九、技术要点总结

| 项目 | 详情 |
|------|------|
| **签名算法** | SHA-256 |
| **签名方式** | 拼接参数后哈希，输出32位小写十六进制 |
| **加密算法** | AES-128-CBC / AES-256-CBC |
| **填充方式** | PKCS7 |
| **IV 向量** | 全零 16 字节 |
| **密钥长度** | 16 字节 (AES-128) / 32 字节 (AES-256) |
| **编码格式** | Base64 |
| **密钥存储** | so 库的 `.data` 段，明文硬编码 |
| **Token 有效期** | 2 小时 (7200秒) |
| **API 基础** | HTTPS POST，JSON 格式 |

---

## 附录：参考文件

| 文件 | 说明 |
|------|------|
| `pateo_crypto.py` | AES 加解密工具库 |
| `checkcode_manager.py` | CheckCode 管理器 |
| `sgmw_token_diagnostic.py` | Token 诊断工具 |
| `pateo_api_client.py` | Python API 客户端 |
| `SignUtils.kt` | Android 签名工具类 |
| `WulingApiClient.kt` | Android API 客户端 |
| `ApiConfig.kt` | Android 配置类 |

