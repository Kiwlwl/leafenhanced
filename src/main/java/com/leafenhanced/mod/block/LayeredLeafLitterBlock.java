package com.leafenhanced.mod.block;

import com.leafenhanced.mod.LeafEnhancedMod;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LayeredLeafLitterBlock extends Block {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 4);
    private static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{
            Shapes.empty(),
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0)
    };

    public LayeredLeafLitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        int layers = state.getValue(LAYERS);
        return layers < LeafEnhancedConfig.get().maxLayers;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return LeafEnhancedConfig.get().leafLitterDisappearance;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!LeafEnhancedConfig.get().leafLitterDisappearance) {
            return;
        }

        int speedRule = level.getGameRules().get(LeafEnhancedMod.LEAVES_DISAPPEARANCE_SPEED);
        if (speedRule <= 0) {
            return;
        }

        int baseTicks = LeafEnhancedConfig.get().disappearanceTicks;
        int scaledTicks = (int) (baseTicks * (100.0 / speedRule));

        if (random.nextInt(Math.max(scaledTicks, 1)) == 0) {
            int layers = state.getValue(LAYERS);
            if (layers <= 1) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(LAYERS, layers - 1), 3);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
        if (!level.getBlockState(pos.below()).isSolid()) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && LeafEnhancedConfig.get().leafLitterShovelCollect) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof ShovelItem) {
                int layers = state.getValue(LAYERS);
                if (layers <= 1) {
                    level.removeBlock(pos, false);
                } else {
                    level.setBlock(pos, state.setValue(LAYERS, layers - 1), 3);
                }
                popResource(level, pos, new ItemStack(LeafEnhancedMod.LAYERED_LEAF_LITTER_ITEM));
            }
        }
    }
}
