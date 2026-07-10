# 落叶增强模组 TODO

## 当前状态

MVP 代码已全部完成，设计文档已同步更新。

**已完成：** 核心功能、配置系统、性能优化、代码审查修复、设计文档更新。

**待验证：** 编译 + 游戏内测试。

## MVP 实现清单

### 代码 ✅
- [x] 模组主入口 + 客户端入口
- [x] 方块: LayeredLeafLitterBlock（4 层，无碰撞，可燃，铲子收集）
- [x] 粒子: FallingLeafParticle + LeafParticleRenderer（受风漂移，渐隐消失，速度上限）
- [x] 配置: LeafEnhancedConfig（ClothConfig 界面 + ModMenu 集成）
- [x] Mixin: LeavesBlockMixin（randomTick @RETURN） + MinecraftMixin（tick）
- [x] 风力: WindState（风向平滑变化，10~30 秒切换）
- [x] 堆积: LeafLitterHandler（biome cache LRU，原版 leaf_litter 兼容）
- [x] biome tag: deciduous_biomes.json（7 种温带群系）

### 性能优化 ✅
- [x] 全局粒子上限 config.maxActiveParticles
- [x] 距离衰减 config.distanceFalloff
- [x] 生成间隔 config.spawnInterval
- [x] Y 偏向上方采样（+4~+16）
- [x] 概率门控（getBiome 前先 random）
- [x] 粒子风速 Mth.clamp
- [x] 配置加载取消多余文件写入

### 设计文档 ✅
- [x] DESIGN.md 同步最新实现
- [x] TODO.md 跟踪
- [x] WIND_IMPROVEMENTS.md 未来风力改进记录

### 清理 ✅
- [x] 移除 gamerule、暴风雨、风声（超出 MVP）
- [x] 移除空 init() 方法、未使用的 import
- [x] 修复 Mth.clamp 下标越界防御、缺失 import
- [x] .gitignore 清理多余规则
- [x] README.md 完善
- [x] 移除 build.gradle 中空的 publishing block

## 下一步

1. **`gradlew build` 验证编译** — 在本机 JDK 25 运行，根据错误修正 API 映射
2. **游戏内测试** — 粒子视觉效果、风力表现、落叶堆积/消失/铲子收集
3. **替换占位贴图** — 把 Python 生成的 4 层贴图换成正式美术资源

## 风险点

- 26.2 比较新，官方映射可能与代码假设有差异
- 未实际编译验证过，部分 API（BlockRenderLayerMap、SimpleSoundInstance 等）可能在 26.2 中已变更
- `SoundType.GRASS` 的脚步声是否贴合落叶质感需进游戏确认

## 未来可扩展

参考 `WIND_IMPROVEMENTS.md` 和 `DESIGN.md` 的"未来可扩展"章节。
