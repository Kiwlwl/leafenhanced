package com.leafenhanced.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LayeredLeafLitterBlockEntity extends BlockEntity {
    private BlockState wrappedState = Blocks.AIR.defaultBlockState();

    public LayeredLeafLitterBlockEntity(BlockPos pos, BlockState state) {
        super(LeafEnhancedMod.LEAF_LITTER_BE, pos, state);
    }

    public BlockState getWrappedState() {
        return wrappedState;
    }

    public void setWrappedState(BlockState state) {
        this.wrappedState = state;
        setChanged();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("wrapped_block", Block.getId(wrappedState));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        wrappedState = Block.stateById(tag.getInt("wrapped_block"));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
}
