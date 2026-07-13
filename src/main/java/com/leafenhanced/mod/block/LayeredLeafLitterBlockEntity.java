package com.leafenhanced.mod.block;

import com.leafenhanced.mod.LeafEnhancedMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("wrapped_block", Block.getId(wrappedState));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getInt("wrapped_block").ifPresent(id -> wrappedState = Block.stateById(id));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("wrapped_block", Block.getId(wrappedState));
        return tag;
    }
}
