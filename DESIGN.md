# 落叶增强模组设计文档

## 概述

一个小而精的原版增强模组，灵感来自 **Snow! Real Magic!**。目标不是新增大量内容，而是把原版已经有的"落叶"效果做得更自然、更有氛围。

原版：
- 樱花群系有持续飘落的粉色花瓣。
- 其他树叶没有持续飘落效果。
- 落叶层只在特定群系自然生成，且不会动态变化。

本模组在保持原版风格的前提下，增强树叶的落叶视觉与地面落叶的堆积、分解。

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
- **只在树叶下方有空气时飘落。**
- **每个树叶方块自身按群系判定**，边界自然过渡。
- 叶子飘落受当前风向与风力影响，产生横向漂移（风在每 tick 持续加速粒子，非一次性初速度）。
- **全局存活粒子数上限**，防止无限累积导致 GPU 压力。
- **距离衰减**，远处粒子概率降低，减少无效生成。
- 粒子生成间隔、寿命、最大风速均可配置。

### 2. 地面落叶层堆积

树叶方块下方有空气时，随机 tick 生成自定义层状落叶方块 `layered_leaf_litter`。

- 最多堆积到 `maxLayers` 层，默认 4 层。
- 每层 2 像素，像原版雪层一样可堆叠。
- **落叶层可穿过，无碰撞**，类似原版 `leaf_litter`。
- 不影响视野、不窒息、不埋人。
- 如果目标位置已有原版 `leaf_litter`，则先替换为 1 层自定义落叶方块，再继续堆积。
- 按群系判定（per-block，带 LRU 缓存）。
- 只影响温带落叶群系，不覆盖沙漠、热带、积雪群系。
- 堆积速度可调（`accumulationSpeed`）。

### 3. 落叶分解

落叶层会在地上停留一段时间后自然消失。

- 消失速度可调（`disappearanceSpeed`）。
- 可以配置为永不消失。
- 消失基准时间可配置（`disappearanceTicks`）。

### 4. 简单交互

1. **铲子收集**
   - 用铲子攻击（左键）落叶层可减少一层并掉落对应物品。
   - 参考原版 `SnowLayerBlock` 的铲子行为。

2. **踩踏音效**
   - 玩家走在落叶层上时播放原版 `SoundType.GRASS` 的沙沙声。

## 技术实现

### 不需要的

- 不需要 Snow Real Magic 的"方块套壳"机制。
- 不需要新增复杂的方块实体来保存原始方块。
- 不需要自定义结构或世界生成。
- 不需要季节系统或天气系统。
- MVP 阶段不需要阵风、暴风雨、落叶散落、成就、gamerule。

### 已实现

1. **客户端粒子**（通过 `MinecraftMixin` 注入 `tick()`）
   - 每 `spawnInterval` tick 在玩家周围 Y+4~+16 范围搜索树叶方块。
   - 找到树叶方块后检查下方是否空气，再按群系标签和概率决定是否生成。
   - 粒子受风力持续加速，带 `Mth.clamp` 限制最大风速。
   - 粒子寿命最后 20 tick 渐隐消失，带旋转动画。

2. **服务端随机 tick**
   - 通过 Mixin `LeavesBlockMixin` 注入 `randomTick` 的 `@At("RETURN")`。
   - 在随机 tick 中检查下方空间，决定是否生成/堆积落叶层。
   - **带 try-catch**，避免 Mixin 异常破坏原版树叶行为。

3. **落叶层方块**
   - 自定义层状方块 `LayeredLeafLitterBlock`，`LAYERS` 属性 1-4 层。
   - 无碰撞，玩家可穿过，只做视觉堆积。
   - 堆积时若遇到原版 `leaf_litter`，先替换为 1 层自定义方块，再继续堆高。
   - 4 层独立模型 + 独立贴图（`leaf_litter_layer_1` ~ `leaf_litter_layer_4`）。
   - 注册为 `cutout` 渲染类型，透明区域不显示黑色背景。
   - `getShape()` 带 `Mth.clamp` 防御下标越界。
   - 方块属性：
     - 声音：`SoundType.GRASS`（原版树叶声）。
     - 硬度：`0.1f`，徒手快速破坏。
     - 不遮光、可穿过实体、可燃。
   - 掉落物：按方块 `layers` 属性数值掉落对应数量。

4. **配置**
   - 使用 ClothConfig 生成配置界面，ModMenu 集成。
   - 配置项分组：

   **落叶（Falling Leaves）：**
   - `fallingLeaves`：是否开启落叶粒子
   - `particleChance`：候选树叶通过概率检查的机会（0~1）
   - `maxParticlesPerTick`：每 tick 最多生成粒子数
   - `particleRenderDistance`：粒子搜索水平半径（格）
   - `spawnInterval`：粒子扫描间隔刻数，越大性能越好
   - `baseWindStrength`：基础风力大小
   - `accumulationSpeed`：落叶堆积速度（0~500，默认 100）
   - `disappearanceSpeed`：落叶消失速度（0~500，默认 100）

   **性能（Performance）：**
   - `distanceFalloff`：距离衰减强度，0=无，1=最大
   - `maxActiveParticles`：全局最大存活粒子数
   - `particleLifetime`：每个粒子存活刻数
   - `particleMaxWindSpeed`：粒子受风加速上限

   **落叶层（Leaf Litter）：**
   - `leafLitterAccumulation`：是否开启落叶堆积
   - `accumulationChance`：基础堆积概率
   - `leafLitterDisappearance`：是否开启落叶消失
   - `maxLayers`：最大堆积层数（1-4）
   - `disappearanceTicks`：消失一层所需基准 tick 数
   - `leafLitterShovelCollect`：是否开启铲子收集

5. **性能优化**
   - **全局粒子上限**：达到 `maxActiveParticles` 时整轮跳过生成。
   - **距离衰减**：远处粒子概率线性降低，最低 20%。
   - **Y 采样偏向上方**：只搜索玩家上方 4~16 格，提高树叶命中率。
   - **概率门控**：`getBiome` 查询前先过 `random.nextDouble()`，平均减少 90% 的不必要查询。
   - **群系缓存**：`LeafLitterHandler` 用 LRU（512 条目）缓存 biome tag 结果。
   - **落叶层只分 4 层**：不实现 8 层，减少方块状态数量。
   - **随机 tick 限制**：堆积和消失都走 random tick，不主动扫描世界。

6. **事件频率**
   - 粒子生成：默认每 5 tick 扫描一次，每次最多 50 次尝试，经概率 + 距离衰减 + 群系过滤后实际生成约 3~8 个粒子/tick。
   - 落叶堆积：树叶 random tick 被选中时（约每 68 秒/方块），按 `accumulationChance × (accumulationSpeed / 100)` 概率触发。
   - 落叶消失：每 random tick 按 `disappearanceTicks × (100 / disappearanceSpeed)` 计算概率。

## 群系与落叶

原版 Minecraft 没有四季，因此本模组不引入季节系统。所有落叶效果以**群系**为主要判定条件：

- 落叶粒子、落叶层堆积都在温带落叶群系中发生。
- 沙漠、热带、积雪等固定气候群系不参与。
- 群系边界自然过渡（每个树叶方块各自判断自身所在群系的 biome tag）。
- `deciduous_biomes.json` 包含：`minecraft:forest`、`minecraft:birch_forest`、`minecraft:old_growth_birch_forest`、`minecraft:dark_forest`、`minecraft:flower_forest`、`minecraft:cherry_grove`、`minecraft:windswept_forest`。
- 樱花林：与其他落叶群系一样生成落叶粒子和落叶层，同时保留原版粉色花瓣效果。

## 风向与风力系统

为了让落叶更有动态感，模组在客户端维护一个全局风状态：

- **风向变化**：随机间隔（10~30 秒）生成新的目标风向，当前风向每 tick 向目标平滑过渡 5%。
- **基础风力**：决定落叶横向漂移速度。
- **全部客户端计算**：不影响服务端方块状态。
- 风风力仅作为粒子的持续性加速度（非生成时的初速度），粒子每 tick 累积风力，带 `Mth.clamp` 上限。

## 粒子生成流程

```
每 tick:
  ├─ level / player 为空？ → 跳过
  ├─ tickCounter % spawnInterval ≠ 0？ → 跳过
  ├─ 配置关闭或 chance ≤ 0？ → 跳过
  ├─ activeCount ≥ maxActiveParticles？ → 跳过
  └─ 循环 maxParticlesPerTick 次:
       ├─ 随机坐标 (X: ±rangeXZ, Y: +4~+16, Z: ±rangeXZ)
       ├─ 是树叶？
       ├─ 下方是空气？
       ├─ 概率判定 (chance × distanceFactor)
       ├─ 群系在 deciduous_biomes 中？
       └─ 全部通过 → 生成粒子
           （初速度仅含随机抖动，不含风力）
```

## 兼容性

- 落叶效果通过 biome tag 限定。
- 不参与固定气候的群系（沙漠、热带、积雪）。
- 纯数据与事件驱动，不修改原版地形生成。
- 服务端与客户端都需要安装。

## 边界情况

- **下方方块被破坏**：落叶层通过 `neighborChanged` 检测，下方方块被破坏时自动掉落。
- **上方有实体/方块**：已有实体或方块的位置不再生成落叶层。
- **区块卸载**：落叶层状态随区块保存，不需要额外处理。
- **火把/光源附近**：落叶层默认可燃，靠近火源会点燃。

## 文件结构

```
leafenhanced/
├── .gitignore
├── LICENSE
├── README.md
├── DESIGN.md
├── TODO.md
├── SNOW_REAL_MAGIC_ANALYSIS.md
├── WIND_IMPROVEMENTS.md
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src/main/java/com/leafenhanced/mod/
│   ├── LeafEnhancedMod.java                  # 模组主入口
│   ├── block/LayeredLeafLitterBlock.java      # 层状落叶方块
│   ├── config/
│   │   ├── LeafEnhancedConfig.java            # 配置数据 + 序列化
│   │   ├── LeafEnhancedConfigScreen.java      # ClothConfig 配置界面
│   │   └── ModMenuIntegration.java            # ModMenu 集成
│   ├── client/
│   │   ├── LeafEnhancedModClient.java         # 客户端入口 + 粒子生成逻辑
│   │   ├── particle/LeafParticleRenderer.java # 粒子渲染 + tick
│   │   └── wind/WindState.java                # 客户端风力系统
│   ├── mixin/
│   │   ├── LeavesBlockMixin.java              # 树叶 randomTick 注入
│   │   └── MinecraftMixin.java                # 客户端 tick 注入
│   ├── particle/FallingLeafParticle.java      # 粒子类型注册
│   ├── tag/ModBiomeTags.java                  # 群系标签键
│   └── world/LeafLitterHandler.java           # 服务端落叶堆积逻辑
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── leafenhanced.mixins.json
│   ├── assets/leafenhanced/
│   │   ├── blockstates/layered_leaf_litter.json
│   │   ├── icon.png
│   │   ├── lang/en_us.json
│   │   ├── lang/zh_cn.json
│   │   ├── models/block/layered_leaf_litter1~4.json
│   │   ├── models/item/layered_leaf_litter.json
│   │   ├── particles/falling_leaf.json
│   │   └── textures/
│   │       ├── block/leaf_litter_layer_1~4.png
│   │       └── particle/falling_leaf.png
│   └── data/leafenhanced/
│       └── tags/worldgen/biome/deciduous_biomes.json
```

## 未来可扩展

- 阵风事件（见 `WIND_IMPROVEMENTS.md`）
- 暴风雨事件
- 落叶散落
- 游戏规则（gamerule）
- 成就
- 光照/雨水影响消失
- 自定义落叶音效
- 落叶层方块套壳（台阶、楼梯、栅栏、墙）
- 不同树种落叶颜色/形状差异
- 落叶层与季节模组兼容
- 落叶隐藏物品、落叶堆隐蔽、落叶层减速玩家
