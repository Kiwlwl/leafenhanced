package com.leafenhanced.mod.world;

import com.leafenhanced.mod.LeafEnhancedMod;
import com.leafenhanced.mod.block.LayeredLeafLitterBlock;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeafLitterHandler {
    private static final Map<Long, Boolean> BIOME_CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
            return size() > 512;
        }
    };

    public static void init() {
    }

    public static void onLeafRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!LeafEnhancedConfig.get().leafLitterAccumulation) {
            return;
        }

        if (!isDeciduousBiome(level, pos)) {
            return;
        }

        int speedRule = level.getGameRules().get(LeafEnhancedMod.LEAVES_ACCUMULATION_SPEED);
        if (speedRule <= 0) {
            return;
        }

        double scaledChance = LeafEnhancedConfig.get().accumulationChance * (speedRule / 100.0);
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
        if (!groundState.isSolid()) {
            return;
        }

        if (groundState.is(LeafEnhancedMod.LAYERED_LEAF_LITTER)) {
            int layers = groundState.getValue(LayeredLeafLitterBlock.LAYERS);
            if (layers < LeafEnhancedConfig.get().maxLayers) {
                level.setBlock(groundPos, groundState.setValue(LayeredLeafLitterBlock.LAYERS, layers + 1), 3);
            }
            return;
        }

        if (groundState.is(Blocks.LEAF_LITTER)) {
            level.setBlock(groundPos, LeafEnhancedMod.LAYERED_LEAF_LITTER.defaultBlockState().setValue(LayeredLeafLitterBlock.LAYERS, 1), 3);
            return;
        }

        BlockPos litterPos = groundPos.above();
        if (level.getBlockState(litterPos).isAir()) {
            level.setBlock(litterPos, LeafEnhancedMod.LAYERED_LEAF_LITTER.defaultBlockState().setValue(LayeredLeafLitterBlock.LAYERS, 1), 3);
        }
    }

    private static BlockPos findGround(LevelReader level, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        for (int i = 0; i < 16; i++) {
            BlockState state = level.getBlockState(pos);
            if (state.isSolid()) {
                return pos.immutable();
            }
            pos.move(Direction.DOWN);
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
