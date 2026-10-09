# GokiStats Reborn — Minecraft 1.20.1 Forge 非官方迁移版

工程位置：`E:\模组\发cf的模组\GokiStats\1201 forge GokiStats`。

基于 InfinityStudio/GokiStats 的 1.18.1 分支提交 `be0046efbe0d2797982225df822479a2f3332caa`，保留原有 21 项已注册技能、升级和退还、玩家存档、网络同步、数据包标签与寻宝掉落表、图标和音效。

- Minecraft：1.20.1。
- Forge：47.4.23。
- Java：17；Gradle Wrapper：8.8。
- 模组 ID：`gokistats_reborn`；迁移版本：`1.4.0-port.1`。
- 默认按键：Y；按住 Ctrl 点击技能可退还一级。
- 管理员命令：`/gokistats_reborn gui`，`/gokistats_reborn gui <玩家>`；旧 `/gokistats` 保留为别名。
- 原版 `/gokistats reload` 未实现热重载；迁移版显示 Forge 配置管理提示，配置更改后重启世界。

## 构建

在本目录使用 PowerShell：

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat build --offline --console=plain
```

首次在没有缓存的设备上构建时去掉 `--offline`，并确保 Forge 官方依赖可下载。
正式 JAR 与源码 JAR 位于 `build/libs`；发布时另附完整工程源码包，源码 JAR 仅包含源码和资源，不代替完整构建工程。

## 功能范围与迁移修正

原版技能清单和默认数值保留，游泳与熔炉技能仍与上游一致未注册。熔炉技能在上游因严重性能问题停用。
迁移了技能注册、Forge 事件、工具标签、伤害来源、掉落 Codec、按键和 GuiGraphics 界面。
修正单人游戏同步、重生能力复制、退还计数、升级请求参数与线程、经验进度计算、伤害来源配置、工具技能误叠加、跳跃轴向、空手基础伤害重复计算和效果倍率重复计算。
生命值改为固定 UUID 的独立属性修饰器，避免清空其他模组的修饰器。掉落处理增加递归保护和有限遍历，避免重复处理新增掉落。
修正技能读取时反复创建整份默认技能表的额外分配；新增英文 debug 日志用于查看技能请求与升级结果。
配置禁用技能后停止相应效果；潜行和格挡状态下升级的加成可更新。
服务端测试进一步修正能力注册事件总线、死亡复制时能力恢复，以及攀爬额外水平位移。

## 改名与旧技能数据

本项目名称为 GokiStats Reborn，模组 ID、网络通道、技能注册及资源命名空间为 `gokistats_reborn`。
玩家技能存储外层键保留 `gokistats:stat_storage`，读取时将旧 `gokistats:<技能>` 映射为新技能；后续保存写入新技能 ID。
服务端配置文件继续使用 `gokistats-server.toml` 与原有 `stat_*.toml`，已有数值不因改名而重置。旧存档键和配置文件名属于有意保留的兼容标识。
替换旧版 JAR 后使用，不能同时装入原版与本迁移版。已有数据包或整合脚本若引用旧资源 ID，需自行更新为新命名空间。
本轮只验证技能 NBT 格式读取兼容，未验证直接迁移完整 1.18.1 世界。

## 验证边界

已完成编译、正式重混淆构建、静态资源检查，以及 Forge 47.4.23 GameTest 服务端中的 26 组功能检查，结果全部通过。
测试覆盖模拟玩家的升级退还、存档 NBT、死亡与末地返回、属性和战斗事件、实际方块掉落、同步包发送与两名模拟玩家的数据隔离。测试环境与正式工程使用相同的 69 个 Java 源文件。
尚未启动真实客户端联机；界面、客户端实际收包、移动手感、跨服务器重启后的真人登录和 1.18.1 存档迁移仍待验证。GameTest 源码运行结果也不代替正式重混淆 JAR 在独立生产服务端的装载测试。具体结果见 SERVER-TEST-REPORT.md，手工补测项目见 TESTING.md。
元数据按目录约定未设置最高版本，但开发和构建目标仅为 Minecraft 1.20.1 Forge，不代表已兼容其他版本。

## 授权与归属

原作者：Lasm Gratel、InfinityStudio / GokiStats Team；非官方迁移维护：hp3081925122。
原项目：https://github.com/InfinityStudio/GokiStats
原发布页：https://www.curseforge.com/minecraft/mc-mods/gokistats

代码为 GPL-3.0，资源为 CC BY-NC-SA 4.0。授权依据及迁移日期见 NOTICE.md、RESOURCE-LICENSE.md、UPSTREAM-README.md 和 LICENSE。
资源的非商业限制需要单独遵守，参与广告、收益计划或其他商业使用前应核对授权，必要时取得原权利人许可。
