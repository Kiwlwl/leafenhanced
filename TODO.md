# 落叶增强模组实现计划

## 当前状态
- 设计文档已确定：`DESIGN.md`
- Snow Real Magic 分析：`SNOW_REAL_MAGIC_ANALYSIS.md`
- 用户不熟悉 Fabric 和 Mixin，但选择完整实现方案（B）。
- 已精简设计，MVP 只保留核心功能 + 简单交互。
- 临时 Mod ID：`leafenhanced`（包名 `com.leafenhanced.mod`），后续可改。
- **MVP 代码已实现**，项目结构和资源文件已就位。
- **构建验证受阻**：当前环境缺少完整 JDK 25 / Gradle 9.5.1 工具链，且 Loom 1.17.13 与 Gradle 9.5 在本环境出现 `disableObfuscation` 兼容性错误，需要在本机安装 Java 25 后执行 `gradlew build` 验证。

## MVP 实现步骤（已完成）

1. **升级项目环境到 Minecraft 26.2**
   - 更新 `build.gradle`（Mojang 官方映射、Loom 1.17-SNAPSHOT、Java 25）
   - 更新 `gradle.properties`（minecraft 26.2, loader 0.19.3, fabric-api 0.154.2+26.2）
   - 更新 `settings.gradle`
   - 添加 Gradle wrapper 9.5.1（`gradle-wrapper.jar` 需在本机联网后通过 `gradle wrapper` 或运行 `download-wrapper.bat` 生成）

2. **创建模组基础结构**
   - `fabric.mod.json`
   - 主类 `LeafEnhancedMod.java`
   - 客户端入口 `LeafEnhancedModClient.java`

3. **配置系统**
   - 手写 JSON 配置 `LeafEnhancedConfig`（未使用 AutoConfig，因为 26.2 版本暂无确认可用的 AutoConfig 发布）
   - 配置项：粒子开关/概率/密度/距离/风速、落叶层开关/概率/消失速度/最大层数、铲子收集

4. **落叶粒子与风力**
   - 自定义粒子 `FallingLeafParticle` / 客户端渲染 `LeafParticleRenderer`
   - 客户端全局风状态 `WindState`
   - 受风力影响横向漂移、旋转、淡出

5. **自定义层状落叶方块**
   - 方块 `LayeredLeafLitterBlock`（类似 `SnowLayerBlock`，无碰撞、可替换、可燃）
   - 物品注册
   - 方块状态、模型（4 层独立模型）
   - 每层独立贴图（Python 生成的占位贴图）
   - 掉落物 loot table
   - 使用原版 `LEAVES` 声音组

6. **树叶 random tick 堆积落叶**
   - Mixin `LeavesBlockMixin` 注入 `randomTick`
   - 群系判定 + 群系缓存
   - 遇到原版 `leaf_litter` 时替换为自定义 1 层方块

7. **落叶消失**
   - 随机 tick 让落叶层直接减少层数或消失（不变成泥土）

8. **群系标签**
   - `deciduous_biomes.json` biome tag（森林、桦木森林、繁花森林、黑森林、樱桃树林等）

9. **简单交互**
   - 铲子左键收集一层落叶
   - 使用原版 `LEAVES` 声音组作为踩踏音效

10. **游戏规则（gamerule）**
    - `leavesAccumulationSpeed`
    - `leavesParticleDensity`
    - `leavesDisappearanceSpeed`

11. **性能优化**
    - 粒子渲染距离、每 tick 最大粒子数
    - 群系缓存
    - 落叶层只分 4 层

## 下一步
- 在本机安装 JDK 25 并运行 `gradlew build` 验证编译。
- 根据编译错误修正映射/类名（26.2 官方映射可能与代码假设有差异）。
- 替换 Python 生成的占位贴图为美术资源。

## 风险点
- 26.2 比较新，文档和示例少。
- 自定义层状方块是最大难点（方块状态、模型、4 张贴图）。
- 不熟悉 Fabric/Mixin 会延长实现时间。
- **Loom 1.17.13 + Gradle 9.5.1 在本环境出现兼容性问题**，可能需要等待 Loom 更新或在本机确认是否正常。

## 未来可扩展
- 阵风事件、暴风雨事件。
- 落叶散落。
- 游戏规则（gamerule）。
- 成就。
- 光照/雨水影响消失。
- 自定义落叶音效（替换原版 LEAVES 声音组）。
- 落叶层方块套壳（台阶、楼梯、栅栏、墙）。
- 不同树种落叶颜色/形状差异。
- 落叶隐藏物品、落叶堆隐蔽、落叶层减速玩家。

## 备注
- 先完成 MVP，跑通核心功能后再考虑未来扩展。
- 不要一次性实现所有功能。
