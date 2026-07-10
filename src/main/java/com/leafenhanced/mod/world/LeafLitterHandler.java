package com.leafenhanced.mod.world;

import com.leafenhanced.mod.LeafEnhancedMod;
import com.leafenhanced.mod.block.LayeredLeafLitterBlock;
import com.leafenhanced.mod.block.LayeredLeafLitterBlockEntity;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import com.leafenhanced.mod.tag.ModBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeafLitterHandler {
    private static final Map<Long, Boolean> BIOME_CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            return size() > 512;
        }
    };

    private static final int leafTypeFromBlock(Block block) {
        if (block == Blocks.OAK_LEAVES) return 0;
        if (block == Blocks.BIRCH_LEAVES) return 1;
        if (block == Blocks.SPRUCE_LEAVES) return 2;
        if (block == Blocks.JUNGLE_LEAVES) return 3;
        if (block == Blocks.ACACIA_LEAVES) return 4;
        if (block == Blocks.DARK_OAK_LEAVES) return 5;
        return 0;
    }

    public static void onLeafRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!LeafEnhancedConfig.get().leafLitterAccumulation) {
            return;
        }

        if (!isDeciduousBiome(level, pos)) {
            return;
        }

        double scaledChance = LeafEnhancedConfig.get().accumulationChance * (LeafEnhancedConfig.get().accumulationSpeed / 100.0);
        if (random.nextDouble() > scaledChance) {
            return;
        }

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (!belowState.isAir()) {
            return;
        }

        BlockPos groundPos = findGround(level, below);
        if (groundPos == null) {
            return;
        }

        BlockState groundState = level.getBlockState(groundPos);
        if (groundState.isAir()) {
            return;
        }

        int leafType = leafTypeFromBlock(state.getBlock());

        if (groundState.is(LeafEnhancedMod.LAYERED_LEAF_LITTER)) {
            int layers = groundState.getValue(LayeredLeafLitterBlock.LAYERS);
            if (layers < LeafEnhancedConfig.get().maxLayers) {
                BlockEntity oldBe = level.getBlockEntity(groundPos);
                BlockState wrapped = Blocks.AIR.defaultBlockState();
                if (oldBe instanceof LayeredLeafLitterBlockEntity leafBE) {
                    wrapped = leafBE.getWrappedState();
                }
                level.setBlock(groundPos, groundState.setValue(LayeredLeafLitterBlock.LAYERS, layers + 1), 3);
                BlockEntity newBe = level.getBlockEntity(groundPos);
                if (newBe instanceof LayeredLeafLitterBlockEntity newLeafBE && !wrapped.isAir()) {
                    newLeafBE.setWrappedState(wrapped);
                }
            }
            return;
        }

        if (groundState.is(Blocks.LEAF_LITTER)) {
            placeWrappedBlock(level, groundPos, groundState, leafType);
            return;
        }

        placeWrappedBlock(level, groundPos, groundState, leafType);
    }

    private static void placeWrappedBlock(ServerLevel level, BlockPos pos, BlockState originalState, int leafType) {
        level.setBlock(pos, LeafEnhancedMod.LAYERED_LEAF_LITTER.defaultBlockState()
                .setValue(LayeredLeafLitterBlock.LAYERS, 1)
                .setValue(LayeredLeafLitterBlock.LEAF_TYPE, leafType), 3);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LayeredLeafLitterBlockEntity leafBE) {
            leafBE.setWrappedState(originalState);
        }
    }

    private static BlockPos findGround(LevelReader level, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int i = 0; i < 16; i++) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                pos.move(Direction.DOWN);
                continue;
            }
            return pos.immutable();
        }
        return null;
    }

    private static boolean isDeciduousBiome(LevelReader level, BlockPos pos) {
        long chunkPos = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);

        Boolean cached = BIOME_CACHE.get(chunkPos);
        if (cached != null) {
            return cached;
        }

        Holder<Biome> biome = level.getBiome(pos);
        boolean result = biome.is(ModBiomeTags.DECIDUOUS_BIOMES);
        BIOME_CACHE.put(chunkPos, result);
        return result;
    }
}
