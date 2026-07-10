package com.leafenhanced.mod.client.block;

import com.leafenhanced.mod.block.LayeredLeafLitterBlock;
import com.leafenhanced.mod.block.LayeredLeafLitterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LayeredLeafLitterRenderer implements BlockEntityRenderer<LayeredLeafLitterBlockEntity> {

    public LayeredLeafLitterRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(LayeredLeafLitterBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        Level level = be.getLevel();
        if (level == null) return;

        BlockState wrappedState = be.getWrappedState();
        if (wrappedState.isAir()) return;

        BlockState leafState = be.getBlockState();
        int layers = leafState.getValue(LayeredLeafLitterBlock.LAYERS);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        pose.pushPose();
        dispatcher.renderSingleBlock(wrappedState, pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();

        float yOffset = (16.0f - layers * 2.0f) / 16.0f;
        pose.pushPose();
        pose.translate(0.0, yOffset, 0.0);
        dispatcher.renderSingleBlock(leafState, pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
