# APK AES 密钥提取报告

> **来源文件**: `new_target2.apk`
> **提取时间**: 2026-08-10
> **分析工具**: 逆向工程 (strings/hex)

---

## 1. AES 密钥

| 属性 | 值 |
|------|-----|
| **算法** | AES-128 (16 字节) |
| **Hex** | `5bfd505c8e4e90c379bb66aa946ea015` |
| **十六进制明细** | `5B FD 50 5C 8E 4E 90 C3 79 BB 66 AA 94 6E A0 15` |
| **存储位置** | libSecretKey.so offset 4104 |

---

## 2. 应用标识信息

| 标识类型 | 值 |
|----------|-----|
| **应用标识** | `HIKVISION_AE` |
| **平台** | `Android` |
| **版本号** | `5594570` |

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

> **注意**: RSA 私钥未在 so 库中找到，可能由云端动态下发或通过 TEE (Trusted Execution Environment) 保护。

---

## 4. SO 库分析

### libSecretKey.so (7,088 bytes)
- **功能**: 密钥存储与管理
- **内容**: AES 密钥、RSA 公钥、应用标识
- **存储格式**: 明文字符串

### libAESUtils.so (20,584 bytes)
- **功能**: AES 加解密实现
- **支持**: AES-128-CBC、AES-128-ECB 模式

### JNI 接口方法
```
Java_com_hikvision_encryptelibrary_AESUtils_AESDecrypt
Java_com_hikvision_encryptelibrary_AESUtils_AESEncrypt
Java_com_hikvision_encryptelibrary_AESUtils_AESEncryptGB2312
Java_com_hikvision_secretkey_SecretKeyUtil_getAESKey
Java_com_hikvision_secretkey_SecretKeyUtil_getAESPublicKey
Java_com_hikvision_secretkey_SecretKeyUtil_getRSAPrivateKey
Java_com_hikvision_secretkey_SecretKeyUtil_getRSAPublicKey
```

---

## 5. Java 层调用示例

```java
// 1. 获取密钥
String aesKey = SecretKeyUtil.getAESKey();
// 返回: "5bfd505c8e4e90c379bb66aa946ea015"

String rsaPublicKey = SecretKeyUtil.getRSAPublicKey();
// 返回 RSA 公钥字符串

// 2. AES 加密
String encrypted = AESUtils.AESEncrypt(plaintext, aesKey);

// 3. AES 解密
String decrypted = AESUtils.AESDecrypt(ciphertext, aesKey);

// 4. GB2312 编码加密
String encryptedGB = AESUtils.AESEncryptGB2312(plaintext, aesKey);
```

---

## 6. 加密算法细节

### AES 加密
- **算法**: AES-128
- **密钥长度**: 16 字节 (128-bit)
- **密钥存储**: 硬编码在 libSecretKey.so 的 .data 段
- **加密模式**: 支持 CBC 和 ECB
- **填充方式**: PKCS5/PKCS7 (推断)

### RSA 加密
- **算法**: RSA
- **密钥长度**: 2048-bit
- **公钥格式**: X.509 SubjectPublicKeyInfo
- **私钥保护**: 可能由云端下发或硬件保护

---

## 7. 密钥存储位置

```
libSecretKey.so 内存布局:
┌─────────────────────────────────┐
│ offset 1354: "SecretKey"        │
│ offset 1556: "aesKey"          │
│ offset 1563: "aesPublicKey"    │
│ offset 1576: "rsaPrivateKey"   │
│ offset 1590: "rsaPublicKey"    │
├─────────────────────────────────┤
│ offset 4104: AES 密钥 (32字节)  │
│              "5bfd505c8e4e90c3│
│               79bb66aa946ea015"│
├─────────────────────────────────┤
│ offset 4137: "HIKVISION_AE"    │
│ offset 4150: RSA 公钥 PEM      │
├─────────────────────────────────┤
│ offset 2456: "Android"         │
│ offset 2532: "5594570"         │
└─────────────────────────────────┘
```

---

## 8. 安全建议

1. **AES 密钥为硬编码**，存在逆向风险
2. **RSA 私钥未存储在本地**，这是良好的安全实践
3. 建议使用动态密钥协商机制替代硬编码密钥
4. 传输层使用 HTTPS/TLS 加密

---

*文档由逆向分析自动生成，仅供安全研究用途*