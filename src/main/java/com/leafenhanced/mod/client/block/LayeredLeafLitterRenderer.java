package com.leafenhanced.mod.client.block;

import com.leafenhanced.mod.block.LayeredLeafLitterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class LayeredLeafLitterRenderer implements BlockEntityRenderer<LayeredLeafLitterBlockEntity, LeafLitterRenderState> {

    public LayeredLeafLitterRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public LeafLitterRenderState createRenderState() {
        return new LeafLitterRenderState();
    }

    @Override
    public void extractRenderState(LayeredLeafLitterBlockEntity be, LeafLitterRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase((BlockEntity) be, state, crumblingOverlay);
    }

    @Override
    public void submit(LeafLitterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
    }
}
