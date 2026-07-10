package com.leafenhanced.mod.block;

import com.leafenhanced.mod.config.LeafEnhancedConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LayeredLeafLitterBlock extends Block implements EntityBlock {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 4);
    public static final IntegerProperty LEAF_TYPE = IntegerProperty.create("leaf_type", 0, 5);

    private static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{
            Shapes.empty(),
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
    };

    public LayeredLeafLitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LAYERS, 1)
                .setValue(LEAF_TYPE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS, LEAF_TYPE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return LeafEnhancedConfig.get().leafLitterDisappearance;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LayeredLeafLitterBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!LeafEnhancedConfig.get().leafLitterDisappearance) {
            return;
        }

        int disappearanceSpeed = LeafEnhancedConfig.get().disappearanceSpeed;
        int scaledTicks = Math.max((int) (LeafEnhancedConfig.get().disappearanceTicks * (100.0 / Math.max(disappearanceSpeed, 1))), 1);

        if (random.nextInt(scaledTicks) == 0) {
            int layers = state.getValue(LAYERS);
            if (layers <= 1) {
                restoreWrappedBlock(level, pos);
            } else {
                BlockState wrapped = getWrappedState(level, pos);
                level.setBlock(pos, state.setValue(LAYERS, layers - 1), 3);
                setWrappedState(level, pos, wrapped);
            }
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && LeafEnhancedConfig.get().leafLitterShovelCollect) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof ShovelItem) {
                int layers = state.getValue(LAYERS);
                if (layers <= 1) {
                    restoreWrappedBlock(level, pos);
                } else {
                    BlockState wrapped = getWrappedState(level, pos);
                    level.setBlock(pos, state.setValue(LAYERS, layers - 1), 3);
                    setWrappedState(level, pos, wrapped);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LayeredLeafLitterBlockEntity leafBE) {
                BlockState wrapped = leafBE.getWrappedState();
                if (!wrapped.isAir()) {
                    level.setBlock(pos, wrapped, 3);
                    return;
                }
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    private void restoreWrappedBlock(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LayeredLeafLitterBlockEntity leafBE) {
            BlockState wrapped = leafBE.getWrappedState();
            if (!wrapped.isAir()) {
                level.setBlock(pos, wrapped, 3);
                return;
            }
        }
        level.removeBlock(pos, false);
    }

    private static BlockState getWrappedState(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LayeredLeafLitterBlockEntity leafBE) {
            return leafBE.getWrappedState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    private static void setWrappedState(Level level, BlockPos pos, BlockState wrapped) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LayeredLeafLitterBlockEntity leafBE && !wrapped.isAir()) {
            leafBE.setWrappedState(wrapped);
        }
    }
}
