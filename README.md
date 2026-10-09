# GokiStats Reborn — Minecraft 1.21.1 NeoForge 迁移版

工程位置：`E:\模组\发cf的模组\GokiStats\1.21.1-neoforge-gokistats-reborn`。

本分支基于 GokiStats Reborn 的 1.20.1 Forge 版本迁移到 Minecraft 1.21.1 NeoForge，保留原有 21 项已注册技能、升级和退还、玩家存档、网络同步、数据包标签与寻宝掉落表、图标和音效。

- Minecraft：1.21.1。
- NeoForge：21.1.233。
- Java：21；Gradle Wrapper：8.8。
- 模组 ID：`gokistats_reborn`；迁移版本：`1.4.0-port.1`。
- 默认按键：Y；按住 Ctrl 点击技能可退还一级；按住左键可连续升级。
- 管理员命令：`/gokistats_reborn gui`，`/gokistats_reborn gui <玩家>`；旧 `/gokistats` 保留为别名。
- 配置文件保留 `gokistats-server.toml` 与 `stat_*.toml`，每项技能的等级上限、每级数值和开关仍由配置控制。

## 构建

在本目录使用 PowerShell：

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-21.0.11'
.\gradlew.bat build --offline --console=plain
```

正式 JAR 与源码工程包位于 `build/libs` 和 `build/distributions`。首次在没有缓存的设备上构建时去掉 `--offline`，并确保 NeoForge 官方依赖可下载。

## 迁移内容

迁移了 NeoForge 的模组元数据、玩家附件存储、网络 Payload、事件总线、属性修饰器、伤害事件、注册表、掉落 Codec、按键和 GuiGraphics 界面，以及 1.21.1 的数据包目录格式。

玩家数据读取兼容旧版 `gokistats:stat_storage` 和旧技能键，迁移后保存使用 `gokistats_reborn` 命名空间；已有配置文件名和数值保持兼容。保留死亡复制、重生、换维度、多人同步、升级退款、长按连续升级、掉落处理和全部技能效果。

上游停用的 Swimming / Furnace Finesse 保持停用。替换旧版 JAR 后使用，不能同时装入原版与本迁移版。

## 服务端验证

已使用独立 NeoForge 1.21.1 GameTest 服务端运行 26 组功能检查，结果为 `passed=26 failed=0`，服务端随后正常关闭。测试覆盖技能注册与附件、旧 NBT 读取、升级退款、等级上限、配置禁用、存档读取、死亡和重生、属性与战斗事件、移动、掉落、网络同步、定时同步和命令注册。完整记录见 [SERVER-TEST-REPORT.md](SERVER-TEST-REPORT.md)。

复现命令：

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-21.0.11'
.\gradlew.bat runGameTestServer -PgokiServerTests --offline --console=plain
```

GameTest 验证的是开发环境中的实际服务端逻辑，没有启动客户端，也没有把重混淆 JAR 装入独立生产服务器；界面显示、客户端实际收包、真实玩家跨版本存档迁移和长时间压力测试仍需另行验证。

## 授权与归属

原作者：Lasm Gratel、InfinityStudio / GokiStats Team；非官方迁移维护：hp3081925122。
原项目：https://github.com/InfinityStudio/GokiStats
原发布页：https://www.curseforge.com/minecraft/mc-mods/gokistats

代码为 GPL-3.0，资源为 CC BY-NC-SA 4.0。授权依据及迁移日期见 NOTICE.md、RESOURCE-LICENSE.md、UPSTREAM-README.md 和 LICENSE。