# GokiStats Reborn 界面皮肤

本次界面改动适用于 Minecraft 1.20.1 Forge，保留 21 个已注册技能及点击升级、Ctrl+点击退还、键盘焦点操作。技能图标保留原有含义。

背景遮罩、主面板、标题栏、经验栏、技能状态框、通用按钮和提示框均使用 PNG。文字、等级和经验继续动态绘制，未烘焙进图片。
贴图路径、源图尺寸、九宫格边距、色调、图标图集布局和界面尺寸由 `assets/gokistats_reborn/gui/skin.json` 指定；Java 中只保留皮肤入口和控件逻辑。
资源重载时读取并验证定义与图片尺寸，渲染时使用缓存；不会每帧读取文件或解析 JSON。

## 通过资源包换肤

1. 解压配套 `gokistats-reborn-ui-template.zip`，保持 `pack.mcmeta` 和 `assets` 在资源包根目录。
2. 替换 `assets/gokistats_reborn/textures/gui/` 内 PNG；默认每张 32×32。替换同尺寸图片不用改 JSON。
3. 使用不同文件名或尺寸时，修改 `assets/gokistats_reborn/gui/skin.json` 对应项的 texture、width、height、sourceX、sourceY、sourceWidth、sourceHeight 和 border。texture 支持完整资源命名空间；sourceX/sourceY/sourceWidth/sourceHeight 表示取样区域；默认使用整张图，背景遮罩仅取纯色中心。border 表示取样区域的九宫格边宽，中心和边缘拉伸，四角保留尺寸。
4. 在 Minecraft 资源包列表启用该包，按 F3+T 重载；已打开的技能界面会按新定义重建布局。

patches 的用途：backdrop 背景遮罩、panel 主面板、header 标题栏、footer 经验栏、tooltip 提示框；button_normal 普通、button_hover 悬停/键盘焦点、button_pressed 按下、button_disabled 禁用/不能退还、button_maximum 满级、button_refund 可退还、button_unaffordable 经验不足。

颜色采用 #AARRGGBB。backdrop 默认半透明色调；其余素材默认不染色。替换图标时使用 icons 中的 texture、width、height、cellWidth、cellHeight、rows、stateColumns、firstId、count 和 states；第一张原图涵盖图标 0–19，第二张从 20 开始。
默认图标原图实际为 512×512，每格 48×48，运行时显示为 24×24；不再依赖旧代码隐式的 256×256 UV 假设。

layout 可调整列数、间距、标题及经验栏高度、图标显示大小和提示框内边距。小窗口整体缩放，鼠标命中区域同步变换；长提示自动换行并限制在画面内，过高提示按可用高度缩放。悬停和键盘焦点均可显示技能详情。
无效定义记录英文错误日志并保留上一份有效定义；首次加载失败使用随模组附带的定义。贴图覆盖本身仍由 Minecraft 资源管理器处理，尺寸不符时请修复资源包再重载。

## 素材与许可

新增界面 PNG 来自 Kenney UI Pack - Pixel Adventure (2.0)：
https://kenney.nl/assets/ui-pack-pixel-adventure
许可为 CC0，可免费修改和分发。原始许可随 JAR 放在 `licenses/kenney-ui-pack-pixel-adventure.txt`，具体原文件编号见 RESOURCE-LICENSE.md。
上游技能图标、音效等继续遵守 CC BY-NC-SA 4.0；本次未将上游资源改为 CC0。代码继续使用 GPL-3.0。

## 验证与手工测试

本轮执行离线 build、正式 JAR/完整源码包资源清单核对、PNG 尺寸和切片范围核对、语言键和格式检查。预览图是使用真实素材生成的离线布局预览，不是游戏截图；未启动客户端，也没有把之前的服务端 GameTest 结果当作 UI 运行验证。

客户端需检查：Y 键打开与 Esc 关闭；普通/悬停/经验不足/禁用/满级/可退还状态；点击升级和 Ctrl+点击退还后的经验与等级同步；提示中的实际整数退款；中英文、屏幕边缘提示、小窗口点击区域、Tab/Enter 焦点操作；启用资源包并 F3+T 后贴图与布局更新。
