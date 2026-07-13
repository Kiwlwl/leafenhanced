# GPU 风模拟研究文档

## 目标

用自定义 Core Shader 在 GPU 上实现树叶的逐顶点风位移，达到真实的风吹树冠效果。

---

## 1. 26.2 渲染管线回顾

```
CPU (每帧)
  LevelRenderer.prepareChunkRenders()
    │
    ├─ 遍历 visibleSections
    │     each → DynamicUniforms$ChunkSectionInfo(
    │               modelView,      ← Matrix4f (每帧新构建)
    │               x, y, z,        ← section 原点 (block coords)
    │               visibility,
    │               textureAtlasWidth, textureAtlasHeight
    │             )
    │
    ├─ DynamicUniforms.writeChunkSections() → GpuBufferSlice[]
    │                                          (GPU SSBO)
    │
    └─ ChunkSectionsToRender(
         chunkSectionInfos = GpuBufferSlice[],
         ...
       )

GPU (每帧)
  DynamicTransforms UBO (std140):
    mat4 ModelViewMat;    ← 来自 ChunkSectionInfo.modelView
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;

  block.vsh:
    vec3 pos = Position + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
```

**关键：** `ModelViewMat` 每帧由 CPU 通过 `GpuBufferSlice` 上传，意味着修改 `ChunkSectionInfo.modelView` 或添加自定义 uniform 就能每帧改变 GPU 上的顶点变换。

---

## 2. 两种 GPU 风实现方案

### 方案 A：修改 modelView 矩阵（无需自定义 shader）

在 `prepareChunkRenders` 中，对含树叶的 section，向 `modelView` 左乘一个风旋转矩阵。

| 优势 | 劣势 |
|------|------|
| 无需 GLSL，纯 Java Mixin | 整个 section 一起动，无波浪感 |
| 每帧自动生效 | 近看不真实，"果冻效应" |
| 实现简单 ~50 行 | 无法区分不同叶片 |

### 方案 B：自定义 vertex shader（逐顶点位移）

编写新的顶点着色器，对每个顶点独立计算风位移。

| 优势 | 劣势 |
|------|------|
| 每片叶子独立飘动 | 需要写 GLSL |
| 波浪传播，风穿过树冠的纹理 | 需要注册 core shader 到 ShaderManager |
| 可叠加湍流、阵风 | 需要处理 shader 重新加载 (F3+T) |

---

## 3. 方案 B 详细设计

### 3.1 需要的文件

```
src/main/resources/assets/leafenhanced/shaders/core/
├── leaf_wind.vsh          # 自定义顶点着色器（核心）
├── leaf_wind.fsh          # 片段着色器（复制 block.fsh，不需改）
└── leaf_wind.json         # Shader 程序定义（如果 26.2 需要）
```

### 3.2 leaf_wind.vsh

基于 `block.vsh` 改造：

```glsl
#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

// ===== 新增：风参数 uniform =====
// 由 LevelEnhancedModClient 每帧通过 UniformUploader 上传
uniform vec3 WindParams;   // (windX, windZ, time)

// ===== 原有输入 =====
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    vec3 pos = Position + ModelOffset;

    // ===== 风位移算法 =====
    // 1. 计算该顶点的相位：不同顶点相位不同 → 波浪
    //    系数 (3.7, 2.3, 1.1) 是质数，确保空间分布均匀
    float phase = pos.x * 3.7 + pos.z * 2.3 + pos.y * 1.1
                + WindParams.z * 1.5;  // 时间驱动动画

    // 2. 风速大小
    float windSpeed = length(WindParams.xy);

    // 3. 基础位移幅度：风速 × 强度因子
    //    sin(phase) 产生 -1 ~ 1 的振荡
    float displacement = sin(phase) * windSpeed * 0.12;

    // 4. 沿风向位移
    pos.x += displacement * WindParams.x;
    pos.z += displacement * WindParams.z;

    // ===== 可选：湍流叠加 =====
    // 第二层高频小幅度噪声
    float turbPhase = pos.x * 13.1 + pos.z * 7.9 + WindParams.z * 3.2;
    float turb = sin(turbPhase) * 0.02 * windSpeed;
    pos.x += turb * WindParams.y;
    pos.z += turb * WindParams.x;

    // ===== 可选：高度风切变 =====
    // 树冠 (Y 高) 位移大，地面 (Y 低) 位移小
    float heightFactor = (pos.y - 4.0) / 32.0;  // 归一化
    heightFactor = clamp(heightFactor, 0.3, 1.0);
    pos.x += displacement * WindParams.x * heightFactor;
    pos.z += displacement * WindParams.z * heightFactor;

    // ===== 标准 MVP 变换 =====
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    // ===== 标准输出 =====
    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
}
```

### 3.3 leaf_wind.fsh

直接复制 `block.fsh`，不需要任何修改：

```glsl
#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd,
        FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
```

### 3.4 算法说明

**基本公式：**
```
displacement = sin(worldPos · noiseVector + time × speed) × windStrength × amplitude
```

**各参数作用：**

| 参数 | 效果 |
|------|------|
| `noiseVector = (3.7, 2.3, 1.1)` | 不同顶点有不同相位，产生波浪图案 |
| `time` | 驱动动画前进，产生风吹流动 |
| `windStrength` | 风越大位移越大 |
| `amplitude = 0.12` | 最大位移幅度（约 0.12 格） |
| `heightFactor` | 树冠动得多，地面动得少 |

**波浪传播原理：**

树冠上相邻的顶点，因为 `pos.x, pos.z` 不同，`phase` 也不同。`sin()` 函数将这些相位映射到 -1~1 振荡，形成视觉上的波浪从树冠一端传播到另一端。

---

## 4. Java 实现计划

### 4.1 Shader 注册

Minecraft 26.2 的 `ShaderManager` 从 `assets/<namespace>/shaders/core/` 加载 shader。
需要在 `ShaderManager` 加载阶段注册 `leaf_wind` 程序。

```java
// 目标：Inject into ShaderManager.apply()
// 在 shader 资源加载完成后，注册 leaf_wind program
// 让 ShaderManager 知道 leaf_wind.vsh / leaf_wind.fsh 这对 shader
```

### 4.2 Uniform 上传

```java
// 目标：Inject into LevelRenderer.render() 或 prepareChunkRenders()
// 使用 UniformUploader 上传 WindParams 到 GPU

// 每帧：
float windX = WindState.windX;
float windZ = WindState.windZ;
float time = (float)(System.currentTimeMillis() / 1000.0);
// 上传到 GPU uniform
GL41.glUniform3f(windUniformLocation, windX, windZ, time);
```

### 4.3 Shader 绑定到树叶 RenderType

让树叶方块（`cutout_mipped` render type）使用 `leaf_wind` shader 而非默认的 `block` shader。

```java
// 目标：修改 RenderType 的 shader program 选择逻辑
// 当 renderType == CUTOUT_MIPPED 且方块是树叶时，使用 leaf_wind program
```

### 4.4 性能优化

- shader uniform 上传是每帧一次，开销极小
- `sin()` 在 GPU 上是单周期指令，数千顶点并行计算
- 无额外 CPU 计算，无 chunk 重建

### 4.5 配置项

```java
// LeafEnhancedConfig 新增
public float windSwayStrength = 0.12f;   // 位移幅度 0.0~0.5
public boolean windTurbulence = true;     // 是否叠加湍流
public float windShearFactor = 0.5f;      // 高度风切变强度
```

---

## 5. Mixin 目标总览

| # | 目标类 | 注入点 | 目的 |
|---|--------|--------|------|
| 1 | `ShaderManager` | `apply()` | 注册 `leaf_wind` shader program |
| 2 | `LevelRenderer` | `render()` 或 `prepareChunkRenders` | 上传 `WindParams` uniform |
| 3 | `RenderType` 或 block rendering dispatch | 选择 shader 处 | 让树叶用 `leaf_wind` |
| 4 | `ShaderManager` | `close()` | 清理 shader 资源 |

或者用 Fabric API 的 `ShaderCallback` 或 `CoreShaderRegistrationCallback`（如果存在）。

---

## 6. 待验证问题

1. **26.2 的 ShaderManager 是否允许动态注册新 shader？**
   - 需要检查 `ShaderManager.apply()` 的加载逻辑
   - 可能需要 Mixin 注入到 `ShaderManager` 的 shader 加载列表

2. **RenderType 与 shader 的映射关系在哪？**
   - 需要找到 `cutout_mipped` render type 引用 shader 的位置
   - 可能通过 `RenderType` 的 `program` 属性，或在 `GameRenderer` 中

3. **Preprocessor 机制是否支持 `ALPHA_CUTOUT` define？**
   - `block.fsh` 中使用了 `#ifdef ALPHA_CUTOUT`
   - 需要确保自己的 shader 也支持这个 define 或直接硬编码 alpha cutout

4. **Uniform 上传的最佳位置？**
   - `LevelRenderer.render()` 中在 chunk rendering 前上传
   - 还是通过 `RenderPass.UniformUploader` 回调？

5. **如何只对树叶应用自定义 shader？**
   - 方案：在 `SectionCompiler` 编译时标记含有树叶的 section
   - 或：在 `BlockStateModelSet` 层面替换 model 的 material/shader
   - 或：替换整个 `CUTOUT_MIPPED` render type 的 shader（影响所有 cutout 方块）

---

## 7. 简化思路（如果 shader 注册太复杂）

如果 26.2 的 shader 注册系统过于封闭，备选方案：

**使用 `PostChain` 后处理效果：**
- 在所有方块渲染完成后，额外 pass 一个风扭曲效果
- 但后处理无法区分树叶 vs 其他方块

**使用 `RenderType` 替换 + `ShaderState` injection：**
- 创建自定义 `RenderType`，使用自定义 `ShaderState`
- 用 Mixin 把树叶的 render type 替换为自定义 render type

**最小可行方案：**
- 跳过自定义 shader，直接用方案 A（修改 modelView 矩阵）
- 效果差一些，但 50 行搞定

---

## 8. 参考

- `block.vsh` → `assets/minecraft/shaders/core/block.vsh`
- `block.fsh` → `assets/minecraft/shaders/core/block.fsh`
- `dynamictransforms.glsl` → `assets/minecraft/shaders/include/dynamictransforms.glsl`
- `projection.glsl` → `assets/minecraft/shaders/include/projection.glsl`
- `ShaderManager` → `net.minecraft.client.renderer.ShaderManager`
- `DynamicUniforms$ChunkSectionInfo` → 包含 `modelView`, `x`, `y`, `z`
- `LevelRenderer.prepareChunkRenders()` → 每帧构建 section 数据的地方
