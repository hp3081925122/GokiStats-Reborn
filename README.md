# GokiStats — Minecraft 1.20.1 Forge 非官方迁移版

工程位置：`E:\模组\发cf的模组\GokiStats\1201 forge GokiStats`。

基于 InfinityStudio/GokiStats 的 1.18.1 分支提交 `be0046efbe0d2797982225df822479a2f3332caa`，保留原有 21 项已注册技能、升级和退还、玩家存档、网络同步、数据包标签与寻宝掉落表、图标和音效。

- Minecraft：1.20.1。
- Forge：47.4.23。
- Java：17；Gradle Wrapper：8.8。
- 模组 ID：`gokistats`；迁移版本：`1.4.0-port.1`。
- 默认按键：Y；按住 Ctrl 点击技能可退还一级。
- 管理员命令：`/gokistats gui`，`/gokistats gui <玩家>`。
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

## 验证边界

已完成编译、正式重混淆构建和静态资源检查。尚未启动客户端或服务端，界面显示、实际技能效果、联机同步和跨版本存档迁移仍需实机验证。
不要把构建成功理解为完整游戏测试通过。具体测试项目见 TESTING.md。
元数据按目录约定未设置最高版本，但开发和构建目标仅为 Minecraft 1.20.1 Forge，不代表已兼容其他版本。

## 授权与归属

原作者：Lasm Gratel、InfinityStudio / GokiStats Team；非官方迁移维护：hp3081925122。
原项目：https://github.com/InfinityStudio/GokiStats
原发布页：https://www.curseforge.com/minecraft/mc-mods/gokistats

代码为 GPL-3.0，资源为 CC BY-NC-SA 4.0。授权依据及迁移日期见 NOTICE.md、RESOURCE-LICENSE.md、UPSTREAM-README.md 和 LICENSE。
资源的非商业限制需要单独遵守，参与广告、收益计划或其他商业使用前应核对授权，必要时取得原权利人许可。
