package com.leafenhanced.mod.client.block;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class LeafLitterRenderState extends BlockEntityRenderState {
    public MovingBlockRenderState wrappedBlock;
    public MovingBlockRenderState leafLayer;
    public int layers;
}
