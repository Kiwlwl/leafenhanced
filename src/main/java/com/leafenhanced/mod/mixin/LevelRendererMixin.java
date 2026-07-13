package com.leafenhanced.mod.mixin;

import com.leafenhanced.mod.client.wind.LeafSectionTracker;
import com.leafenhanced.mod.client.wind.WindState;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.SectionPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Redirect(
        method = "prepareChunkRenders",
        at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0)
    )
    private boolean redirectAddChunkSectionInfo(List<DynamicUniforms.ChunkSectionInfo> list, Object element) {
        DynamicUniforms.ChunkSectionInfo info = (DynamicUniforms.ChunkSectionInfo) element;
        LeafEnhancedConfig config = LeafEnhancedConfig.get();
        if (config.leafSwayStrength > 0.0f && LeafSectionTracker.contains(SectionPos.asLong(info.x(), info.y(), info.z()))) {
            Matrix4f modified = new Matrix4f(info.modelView());
            float windX = WindState.windX * config.leafSwayStrength * 0.5f;
            float windZ = WindState.windZ * config.leafSwayStrength * 0.5f;
            float phase = (info.x() * 3.7f + info.y() * 1.1f + info.z() * 2.3f) * 0.01f;
            float sway = (float) Math.sin(phase) * 0.5f + 0.5f;
            float angleX = windZ * sway * 0.002f;
            float angleZ = windX * (1.0f - sway) * 0.002f;
            modified.rotateLocalX(angleX);
            modified.rotateLocalZ(angleZ);
            info = new DynamicUniforms.ChunkSectionInfo(modified, info.x(), info.y(), info.z(), info.visibility(), info.textureAtlasWidth(), info.textureAtlasHeight());
        }
        return list.add(info);
    }
}
