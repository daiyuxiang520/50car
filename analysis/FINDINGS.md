# 50car APP 逆向分析报告(关键发现)

## 目标文件
- `d8fde25a79e56309a545cd486315478f.zip.7z`(官方加固壳脱壳后的 dump,加壳确认)
  - 外层 7z 密码:`dump`
  - 内层实为 ZIP(扩展名伪装 .7z),含 `classes.dex`(12.8 MB / 9859 类)、`classes2.dex`(3.1 MB / 2230 类)
  - 应用包名:`com.wuling.app`,主类 `com.wuling.app.WulingApplication`(Hilt)

## 应用性质
**第三方五菱新能源车控制 APP(独立开发者作品,非官方)**,用户协议自述:
- 车辆状态监控(电量/续航/里程/胎压/车门车窗车灯等)
- 远程控车(空调/门锁/车窗/后备箱/灯光/鸣笛寻车/启停充电)
- 预约充电、哨兵模式视频查看下载、桌面小组件(2×2、4×4)、摇晃控车、多车管理
- 登录:账号密码 / Token / **激活码激活**
- 自述"无广告、无内购、无付费内容",但存在 **年卡 / 永久赞助版激活码**(邀请返利:好友激活年卡/永久赞助可获得奖励)

## 技术栈
- UI:Jetpack Compose + **Material 3**(androidx.compose.material3 546 类)— 已是谷歌 MD3 体系
- DI:Hilt;存储:DataStore Preferences;JSON: gson
- 加密:Google Tink(com.google.crypto)+ com.flyfish233.crypto
- 网络/通道:Eclipse Paho(**MQTT**,110 类)、com.flyfishxu.kadb(**ADB 客户端**,122 类)
- org.lsposed.hiddenapibypass(隐藏 API 绕过);androidx.work 后台任务

## 激活机制(机制级描述,不含可利用细节)
- 数据模型:`ActivationInfo(activated=…, activatedAt=…, activationCode=…, expiresAt=…?)`
- 相关文案:`* 请使用激活码激活账号`、`* 到期后需要重新激活`、
  "激活码为本 APP 个人使用授权凭证…禁止转售/共享/非官方渠道获取;违规可被封禁"
- 用户协议明确禁止:"破解、绕过 APP 激活机制,使用盗版、修改版安装包"
- 代码经 R8 混淆(A~Z 单字母类名),原版带加固壳

## 结论
"让 APP 一直处于激活状态" = 绕过/伪造激活码授权校验 → 属于规避开发者许可机制,不予实现。
合法替代:官方渠道获取永久版/续期年卡;或做不动授权体系的可靠性改进(登录态持久化、断线重连、后台保活)。
