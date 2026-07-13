package com.leafenhanced.mod.client.block;

import com.leafenhanced.mod.block.LayeredLeafLitterBlock;
import com.leafenhanced.mod.block.LayeredLeafLitterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LayeredLeafLitterRenderer implements BlockEntityRenderer<LayeredLeafLitterBlockEntity, LeafLitterRenderState> {

    public LayeredLeafLitterRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    private static MovingBlockRenderState createMovingBlock(BlockPos pos, BlockState state, ClientLevel level) {
        MovingBlockRenderState renderState = new MovingBlockRenderState();
        renderState.randomSeedPos = pos;
        renderState.blockPos = pos;
        renderState.blockState = state;
        renderState.biome = level.getBiome(pos);
        renderState.cardinalLighting = level.cardinalLighting();
        renderState.lightEngine = level.getLightEngine();
        return renderState;
    }

    @Override
    public LeafLitterRenderState createRenderState() {
        return new LeafLitterRenderState();
    }

    @Override
    public void extractRenderState(LayeredLeafLitterBlockEntity be, LeafLitterRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase((BlockEntity) be, state, crumblingOverlay);
        ClientLevel level = (ClientLevel) be.getLevel();
        BlockPos pos = be.getBlockPos();

        BlockState wrapped = be.getWrappedState();
        if (!wrapped.isAir()) {
            state.wrappedBlock = createMovingBlock(pos, wrapped, level);
        } else {
            state.wrappedBlock = null;
        }

        BlockState leafState = be.getBlockState();
        state.layers = leafState.getValue(LayeredLeafLitterBlock.LAYERS);
        float yOffset = (16.0f - state.layers * 2.0f) / 16.0f;
        BlockPos leafPos = pos.offset(0, yOffset > 0 ? 0 : -1, 0);
        state.leafLayer = createMovingBlock(leafPos, leafState, level);
    }

    @Override
    public void submit(LeafLitterRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (state.wrappedBlock != null) {
            collector.submitMovingBlock(poseStack, state.wrappedBlock, 0);
        }
        poseStack.pushPose();
        float yOffset = (16.0f - state.layers * 2.0f) / 16.0f;
        poseStack.translate(0.0, yOffset, 0.0);
        collector.submitMovingBlock(poseStack, state.leafLayer, 0);
        poseStack.popPose();
    }
}
