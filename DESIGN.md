# 落叶增强模组设计文档

## 概述

一个小而精的原版增强模组，灵感来自 **Snow! Real Magic!**。目标不是新增大量内容，而是把原版已经有的“落叶”效果做得更自然、更有氛围。

原版例子：
- 樱花群系有持续飘落的粉色花瓣。
- 其他树叶没有持续飘落效果。
- 落叶层只在特定群系自然生成，且不会动态变化。

本模组希望在保持原版风格的前提下，增强树叶的落叶视觉与地面落叶的堆积、分解。

## 设计原则

1. **小而精**：一次只改一个核心机制（落叶），不扩展新维度、新装备、新生物。
2. **可开关**：每个功能都有配置项，可以单独关闭。
3. **原版风格**：不破坏原版的视觉与玩法体验。
4. **低性能开销**：避免在每个 tick 扫描大量方块，优先用随机 tick 与客户端粒子事件。
5. **无季节系统**：原版 Minecraft 没有四季，本模组也不引入季节。落叶效果按群系判定。

## 最小可行版本（MVP）

MVP 只实现核心功能，先跑通再考虑扩展。

### 1. 落叶粒子

让落叶型树叶（橡木、桦木、深色橡木、金合欢、丛林木等）持续产生飘落粒子。

- MVP 使用统一的落叶粒子贴图与颜色，不区分树种。
- 只在树叶下方有空气时飘落。
- **按群系判定**，但检查周围小范围，让群系边界自然过渡。
- 参考樱花花瓣的实现，但扩展应用到更多树叶。
- 叶子飘落会受当前风向与风力影响，产生横向漂移。
- 可通过配置限制每 tick 最大粒子数和渲染距离，避免低配置电脑卡顿。

### 2. 地面落叶层堆积

树叶方块下方有空气时，随机 tick 生成自定义层状落叶方块 `layered_leaf_litter`。

- 最多堆积到 `maxLayers` 层，建议最多 4 层（约膝盖高度）。
- 每层 2 像素，像原版雪层一样可堆叠。
- **落叶层可穿过，无碰撞**，类似原版 `leaf_litter`。
- 不影响视野、不窒息、不埋人。
- 如果目标位置已有原版 `leaf_litter`，则先替换为 1 层自定义落叶方块，再继续堆积。
- 按群系判定，但检查周围小范围，让群系边界过渡自然。
- 只影响温带落叶群系，不覆盖沙漠、热带、积雪群系。

### 3. 落叶分解

落叶层会在地上停留一段时间后自然消失。

- 消失速度可调。
- 可以配置为永不消失。

### 4. 简单交互

1. **铲子收集**
   - 用铲子右键落叶层可减少一层并掉落对应物品。
   - 参考原版 `SnowLayerBlock` 的铲子行为。

2. **踩踏音效（MVP 用原版）**
   - 玩家走在落叶层上时播放原版 `LEAVES` 声音组的沙沙声。
   - 自定义落叶音效放到未来扩展。

## 技术实现

### 不需要的

- 不需要 Snow Real Magic 的“方块套壳”机制。
- 不需要新增复杂的方块实体来保存原始方块。
- 不需要自定义结构或世界生成。
- 不需要季节系统或天气系统。
- MVP 阶段不需要阵风、暴风雨、落叶散落、成就、gamerule。

### 需要的

1. **客户端粒子**（`ClientTickEvents` / 方块 `animateTick`）
   - 在树叶的 `animateTick` 中生成自定义落叶粒子。
   - 粒子受风力影响横向漂移。

2. **服务端随机 tick**
   - 通过 Mixin 或 Fabric 事件修改 `LeavesBlock` 的 `randomTick` 行为。
   - 在随机 tick 中检查下方空间，决定是否生成/堆积落叶层。

3. **落叶层方块**
   - 使用自定义层状方块 `layered_leaf_litter`，复用原版 `SnowLayerBlock` 的 `LAYERS` 属性机制。
   - 最多 `maxLayers` 层，默认建议 4 层（膝盖高度），不实现完整 8 层以简化方块状态。
   - **无碰撞**，玩家可穿过，只做视觉堆积。
   - 堆积时若遇到原版 `leaf_litter`，先替换为 1 层自定义方块，再继续堆高。
   - 落叶层模型采用**方案 C**：为每层单独绘制贴图，4 层共 4 张贴图，效果最好。
   - 方块属性：
     - 声音：原版 `LEAVES` 声音组。
     - 硬度/抗性：类似原版树叶，很软，徒手快速破坏。
     - 不遮光、可穿过实体。
     - 渲染类型：`cutout`。
     - 可燃：是。
     - 铲子可快速采集。
   - 默认可燃（不需要配置，按原版树叶预期行为）。
   - 掉落物：破坏后掉落自身物品（或未来改为原版 `leaf_litter`）。

4. **配置**
   - 使用 **AutoConfig** 生成配置界面，配合 **ModMenu** 在模组列表中显示配置按钮。
   - 配置项按类别分组：
      - **粒子**：
        - `fallingLeaves`：是否开启落叶粒子
        - `particleChance`：每个树叶方块每 tick 生成粒子的概率
        - `maxParticlesPerTick`：每 tick 最多生成落叶粒子数
        - `particleRenderDistance`：粒子渲染距离（格）
        - `baseWindStrength`：基础风力大小
      - **落叶层**：
        - `leafLitterAccumulation`：是否开启落叶堆积
        - `accumulationChance`：树叶 random tick 触发堆积的概率
        - `leafLitterDisappearance`：是否开启落叶消失
        - `maxLayers`：落叶层最大堆积层数（建议 4）
        - `disappearanceTicks`：落叶层消失一层所需 tick 数
      - **交互**：
        - `leafLitterShovelCollect`：是否开启铲子收集落叶

5. **性能优化**
   - **粒子渲染距离**：只在玩家周围一定范围内生成粒子，并采用 LOD（远处稀疏、近处密集）。
   - **群系缓存**：每个区块缓存 biome tag 结果，避免每个 random tick 重复查询。
   - **落叶层只分 4 层**：不实现 8 层，减少方块状态数量。
   - **随机 tick 限制**：落叶堆积和消失都走 random tick，不主动扫描世界。
   - **事件合并**：落叶层堆积生成请求合并处理，每 tick 限制最大更新方块数，避免瞬间大量方块变更导致卡顿。

6. **事件频率**
   - 落叶粒子生成概率：每 tick 每个可视树叶方块有 10% 概率生成 1 个粒子（可配置 `particleChance`）。粒子密度适中，营造持续但不杂乱的氛围。
   - 落叶层堆积概率：树叶 random tick 被选中时，有 5% 概率触发堆积（可配置 `accumulationChance`）。堆积缓慢，让玩家自然注意到地上落叶慢慢变多。
   - 落叶消失速度：默认 36000 tick（约 30 分钟游戏时间）消失一层，可配置 `disappearanceTicks`。落叶不会瞬间消失，增强沉浸感。


## 群系与落叶

原版 Minecraft 没有四季，因此本模组不引入季节系统。所有落叶效果以**群系**为主要判定条件：

- 落叶粒子、落叶层堆积都在温带落叶群系中发生。
- 沙漠、热带、积雪等固定气候群系不参与。
- 群系交界处做小范围柔和检查，避免效果突然中断。
- 玩家进入落叶林、桦木林、黑森林、繁花森林、樱花林等群系时，会看到落叶效果。
- 樱花林：与其他落叶群系一样生成落叶粒子和落叶层，同时保留原版粉色花瓣效果。
- `deciduous_biomes.json` 初始包含：`minecraft:forest`、`minecraft:birch_forest`、`minecraft:old_growth_birch_forest`、`minecraft:dark_forest`、`minecraft:flower_forest`、`minecraft:cherry_grove`、`minecraft:windswept_forest`。

## 风向与风力系统

为了让落叶更有动态感，模组在客户端维护一个全局风状态：

- **基础风力**：决定常态下落叶子的横向漂移速度。
- **风向**：决定叶子飘动的方向（东/南/西/北，或任意角度）。
- **全部客户端计算**：不影响服务端方块状态，服务器不感知风向。多人游戏中不同玩家看到的粒子飘动可能略有差异，不影响核心 gameplay。

## 兼容性

- 落叶效果主要影响温带群系，通过 biome tag 限定，并在群系边界做柔和处理。
- 不参与固定气候的群系（沙漠、热带、积雪）。
- 纯数据与事件驱动，不修改原版地形生成，降低与其他地形/群系模组的冲突概率。
- 服务端与客户端都需要安装，因为落叶逻辑涉及服务端方块状态与客户端粒子。

## 边界情况

- **下方方块被破坏**：落叶层下方方块被破坏时，落叶层应像原版雪一样掉落或消失。
- **上方有实体/方块**：已有实体或方块的位置不再生成落叶层，避免覆盖。
- **区块卸载**：落叶层状态随区块保存，不需要额外处理。
- **火把/光源附近**：落叶层默认可燃，靠近火源会点燃，与普通可燃方块行为一致。

## 文件结构规划

```
MyMod/
├── build.gradle              # 26.2 + Fabric Loader + 官方映射
├── gradle.properties
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/leafenhanced/mod/
│   │   │   ├── LeafEnhancedMod.java
│   │   │   ├── config/LeafEnhancedConfig.java
│   │   │   ├── block/LayeredLeafLitterBlock.java
│   │   │   ├── particle/FallingLeafParticle.java
│   │   │   ├── mixin/LeavesBlockMixin.java
│   │   │   └── world/LeafLitterHandler.java
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       ├── leafenhanced.mixins.json
│   │       ├── assets/leafenhanced/
│   │       │   ├── blockstates/
│   │       │   ├── models/block/
│   │       │   ├── textures/block/
│   │       │   ├── particles/
│   │       │   └── textures/particle/
│   │       └── data/
│   │           └── leafenhanced/tags/worldgen/biome/
│   │               └── deciduous_biomes.json
│   └── client/
│       └── java/com/leafenhanced/mod/client/
│           ├── LeafEnhancedModClient.java
│           └── wind/WindState.java
└── SNOW_REAL_MAGIC_ANALYSIS.md
```

## 未来可扩展

- **阵风事件**：随机触发，短时间增加落叶粒子。
- **暴风雨事件**：下雨时落叶效果大幅增强。
- **落叶散落**：高落叶堆在刮风/下雨时向周围散开。
- **游戏规则（gamerule）**：`leavesAccumulationSpeed`、`leavesParticleDensity`、`leavesDisappearanceSpeed`。
- **成就**：如“秋风萧瑟”。
- **光照/雨水影响消失**：阴暗处消失更快/更慢，下雨略微加快/减慢。
- **自定义落叶音效**：玩家走在落叶层上时播放自定义沙沙声，替代原版 `LEAVES` 声音组。
- **落叶层方块套壳**：让落叶可以堆积在台阶、楼梯、栅栏、墙上，类似 Snow Real Magic 的方块套壳机制。
- 不同树种落叶颜色/形状差异。
- 落叶层与季节模组（Serene Seasons / Fabric Seasons）兼容。
- **落叶隐藏物品**：物品掉在厚落叶堆里，会被落叶模型视觉覆盖一部分，看起来像半埋在落叶中。物品仍是正常实体，5 分钟后自然消失，不需要额外控制数量。
- **落叶堆隐蔽**：玩家潜行在落叶堆里降低怪物发现概率。
- **落叶层减速玩家**：玩家穿过厚落叶堆时移动速度略微降低。
