# 海康威视 APK 密钥提取文档

> **来源文件**: `lib/arm64-v8a/libSecretKey.so`、`lib/arm64-v8a/libAESUtils.so`
> **提取时间**: 2026-08-10
> **APK 包名**: com.hikvision.encryptelibrary

---

## 1. AES 密钥

| 属性 | 值 |
|------|-----|
| **算法** | AES-128 (16 字节) |
| **Hex** | `5bfd505c8e4e90c379bb66aa946ea015` |
| **用途** | 数据加解密主密钥 |

### 密钥十六进制明细
```
5B FD 50 5C 8E 4E 90 C3 79 BB 66 AA 94 6E A0 15
```

---

## 2. 应用标识信息

| 标识类型 | 值 |
|----------|-----|
| **平台标识** | `Android` |
| **版本号/设备号** | `5594570` |
| **应用标识** | `HIKVISION_AE` |

---

## 3. RSA 公钥 (2048-bit)

```
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCh5L+PPGq+bwCLOAlGPuSxQT59
VRr7g2So+VVsRD8JWF/fQB2YSS2pVZrBQYTSZsoWdFty4IKTWSWyc6NsukwllQLP
L/UPzDzEN8IXu2Mvz+mnAlGEA9YasykjFpy6Uk+YEBcK1dmCmMcES3oW3DoWwZnk
dV0iq7oVV1eP6cJCSwIDAQAB
-----END PUBLIC KEY-----
```

---

## 4. SO 库功能说明

| 库文件 | 功能 |
|--------|------|
| `libSecretKey.so` | 密钥存储与管理，提供 JNI 接口获取 AES/RSA 密钥 |
| `libAESUtils.so` | AES 加解密实现，支持 AES-128-CBC/ECB 模式 |

### libAESUtils.so 导出的 JNI 方法
- `Java_com_hikvision_encryptelibrary_AESUtils_AESDecrypt` - AES 解密
- `Java_com_hikvision_encryptelibrary_AESUtils_AESEncrypt` - AES 加密
- `Java_com_hikvision_encryptelibrary_AESUtils_AESEncryptGB2312` - GB2312 编码 AES 加密

### libSecretKey.so 导出的 JNI 方法
- `Java_com_hikvision_secretkey_SecretKeyUtil_getAESKey` - 获取 AES 密钥
- `Java_com_hikvision_secretkey_SecretKeyUtil_getAESPublicKey` - 获取 AES 公钥
- `Java_com_hikvision_secretkey_SecretKeyUtil_getRSAPublicKey` - 获取 RSA 公钥
- `Java_com_hikvision_secretkey_SecretKeyUtil_getRSAPrivateKey` - 获取 RSA 私钥

---

## 5. 加密算法细节

### AES 加密
- **算法**: AES-128
- **密钥长度**: 16 字节 (128-bit)
- **密钥存储**: 硬编码在 `libSecretKey.so` 的 `.data` 段中
- **加密模式**: 支持 CBC 和 ECB (从符号表推断)
- **填充方式**: PKCS5/PKCS7 (推断)

### RSA 加密
- **算法**: RSA
- **密钥长度**: 2048-bit
- **公钥格式**: X.509 SubjectPublicKeyInfo
- **私钥**: 未在 SO 库中找到，可能由云端动态下发或 TEE/SE 保护

---

## 6. Java 层调用示例

```java
// 获取 AES 密钥
String aesKey = SecretKeyUtil.getAESKey();
// 返回: "5bfd505c8e4e90c379bb66aa946ea015"

// AES 加密
String encrypted = AESUtils.AESEncrypt(plaintext, aesKey);

// AES 解密
String decrypted = AESUtils.AESDecrypt(ciphertext, aesKey);
```

---

## 7. 注意事项

1. **RSA 私钥未在 SO 中找到**，可能通过以下方式保护：
   - 云端动态下发
   - Android Keystore / TEE (Trusted Execution Environment)
   - SE (Secure Element) 硬件

2. **AES 密钥为硬编码**，存在逆向风险，建议在生产环境中使用动态密钥

3. **加密库为海康威视 SDK**，版本信息：
   - `Android (5220042 based on r346389c)`
   - `clang version 8.0.7`
   - `LLVM 8.0.7svn`

---

*文档由逆向分析自动生成，仅供安全研究用途*