# GokiStats 1.20.1 Forge 服务端测试记录

日期：2026-10-09。结果：**24 组功能检查全部通过，失败 0**。一个 GameTest 测试入口依次执行 24 组检查；Forge 报告 `All 1 required tests passed :)`，Gradle 正常退出并显示 `BUILD SUCCESSFUL`。

## 环境和方法

- Java：Microsoft OpenJDK 17.0.18；Minecraft 1.20.1；Forge 47.4.23；Gradle 8.8。
- 模组：`gokistats`，`1.4.0-port.1`，21 项上游已注册技能。
- 隔离目录：`D:\AI临时文件\gokistats-server-test\project`。正式工程和测试副本的 69 个生产 Java 源文件逐个 SHA-256 一致。
- 启动任务：`runGameTestServer --offline --console=plain`，实际加载服务端世界、配置、注册表、Forge 事件总线和掉落表。
- 玩家采用 Forge FakePlayer 的测试子类；升级调用正式服务端包处理器并使用真实服务器线程上下文。登录、换维度、战斗、跳跃、重生与玩家 tick 通过事件总线触发。方块掉落调用实际 `Block.getDrops`。
- 同步通过内存 Netty 通道截取正式 `PacketDistributor.PLAYER` 发出的数据包，检查目标、消息类型与内容；没有真实 Minecraft 客户端。
- 测试用配置在内存中隔离，强制随机技能触发后恢复原值。测试类和空结构模板只在隔离副本，不加入正式模组 JAR。
- ForgeGradle 的外部连通性探测阻塞后，使用 JVM 参数 `-Dnet.minecraftforge.gradle.check.certs=false` 跳过该构建检查。整个任务使用已缓存依赖的离线模式。

## 本轮修复

1. 能力注册原先订阅 Forge 总线，收不到 `RegisterCapabilitiesEvent`。改为模组总线，`CapabilityStat.STAT.isRegistered()` 已通过实际测试。
2. 玩家能力失效会永久使旧 `LazyOptional` 失效，`reviveCaps()` 不会重建它，导致死亡复制丢等级和退还数。现在失效时通知旧引用，恢复读取时重新创建包装并保留存储。死亡复制、存储独立和完整重生掉级/回血链路均通过。
3. 攀爬额外移动原先包含水平 X/Z 分量，重复增加水平位移。改为只增加垂直分量，潜行停止加速。

## 功能检查明细

| 检查 | 内容 | 结果 |
| --- | --- | --- |
| `registry_and_tags` | 技能注册、工具标签、声音和掉落序列化器 | 通过 |
| `capability_registered` | 能力注册到正确的模组事件总线 | 通过 |
| `capability_attached` | 玩家能力包含21项技能 | 通过 |
| `upgrade_refund_and_experience` | 升级扣费、退还经验与退还计数 | 通过 |
| `invalid_requests_and_limits` | 非法请求、满级和经验不足 | 通过 |
| `refund_limit_and_disabled_skill` | 退还上限与禁用技能 | 通过 |
| `player_nbt_save_load` | 玩家 NBT 保存/读取等级和退还数 | 通过 |
| `clone_copies_independent_storage` | 失效能力恢复、死亡复制及状态独立 | 通过 |
| `health_preserves_other_modifiers` | 生命值加成不删除其他模组修饰器 | 通过 |
| `death_loss_and_end_return` | 死亡掉级开关、比例与末地返回 | 通过 |
| `tool_speed_isolation` | 工具加成隔离 | 通过 |
| `combat_pugilism_sword_and_reaper` | 空手、剑伤与强制收割触发 | 通过 |
| `fire_fall_explosion_and_mob_protection` | 火焰、摔落、爆炸、生物伤害减免 | 通过 |
| `jump_axes_and_sneak_modifier` | 跳跃轴向与潜行加成刷新/移除 | 通过 |
| `bow_arrow_damage` | 弓箭伤害加成 | 通过 |
| `shield_knockback_resistance` | 盾牌格挡抗击退加成刷新/移除 | 通过 |
| `roll_and_excluded_damage` | 翻滚触发、火焰及实际 bypasses_effects 标签排除 | 通过 |
| `climbing_vertical_only_and_sneak` | 攀爬只增加垂直移动，潜行时停止 | 通过 |
| `death_clone_respawn_health_chain` | 死亡复制至重生完整链路与回血 | 通过 |
| `login_dimension_and_multiplayer_sync` | 登录/换维度同步包与两名模拟玩家隔离 | 通过 |
| `network_snapshot_roundtrip` | 技能同步快照编解码 | 通过 |
| `real_block_loot_treasure_and_magician` | 500次真实泥土掉落、寻宝和强制采矿魔法 | 通过 |
| `periodic_sync_tick_handler` | 定时同步处理器与禁用选项 | 通过 |
| `commands_registered` | 命令根节点和玩家目标参数注册 | 通过 |

## 证据与复现

成功日志：`server-test-passed.log`。末尾摘要为 `GOKI_SERVER_TEST RESULT passed=24 failed=0`、`All 1 required tests passed :)` 和 `BUILD SUCCESSFUL`，随后保存世界并正常退出。

测试入口源文件：`D:\AI临时文件\gokistats-server-test\project\src\main\java\net\infstudio\goki\servertest\GokiServerTests.java`。

在隔离工程目录用 PowerShell 复现：

```powershell
$env:JAVA_HOME='C:\Users\30819\.jdks\ms-17.0.18'
$env:JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US -Dnet.minecraftforge.gradle.check.certs=false'
.\gradlew.bat runGameTestServer --offline --console=plain
```

首轮发现真实缺陷后已修复重测；其余测试设置修正包括模拟玩家潜行姿态、事件对象不可重复投递、配置自动保存干扰、内存通道和实际伤害标签选择。当前所有检查均运行通过，未跳过失败项。

## 验证边界

- 这是 Forge 源码开发环境中的实际服务端功能测试，未装载发布用的重混淆 JAR 到独立生产服务端。
- 没有真实客户端连接，未验证界面、声音视觉效果、客户端收到同步后的显示、移动操作手感或真实双人联机。
- NBT 保存读取已测；未验证关闭服务器后真实玩家重新登录、跨版本旧世界迁移或其他模组整合兼容性。
- Combat 检查观察正式伤害事件处理结果；概率技能采用高倍率强制触发，未统计正常倍率下的触发分布。
- 定时同步检查处理器和计数器；未进行长时间压力测试。命令检查注册结构，未验证客户端界面打开。
- 工具检查验证全部工具标签与采矿速度隔离；没有逐个实测所有工具对所有方块的交互。
- 上游已停用的 Swimming / Furnace Finesse 保持停用。
