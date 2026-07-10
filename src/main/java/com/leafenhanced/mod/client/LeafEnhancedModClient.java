package com.leafenhanced.mod.client;

import com.leafenhanced.mod.client.particle.LeafParticleRenderer;
import com.leafenhanced.mod.client.wind.WindState;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import com.leafenhanced.mod.particle.FallingLeafParticle;
import com.leafenhanced.mod.tag.ModBiomeTags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;

public class LeafEnhancedModClient implements ClientModInitializer {
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ParticleProviderRegistry.getInstance().register(FallingLeafParticle.TYPE, new LeafParticleRenderer.Provider());
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        RandomSource random = level.getRandom();

        WindState.update(minecraft, random);

        if (++tickCounter % 5 != 0) {
            return;
        }

        LeafEnhancedConfig config = LeafEnhancedConfig.get();
        if (!config.fallingLeaves || config.particleChance <= 0) {
            return;
        }

        BlockPos center = player.blockPosition();
        Holder<Biome> biome = level.getBiome(center);
        if (!biome.is(ModBiomeTags.DECIDUOUS_BIOMES)) {
            return;
        }

        int attempts = config.maxParticlesPerTick;
        double chance = config.particleChance;

        if (attempts <= 0) {
            return;
        }

        int rangeXZ = config.particleRenderDistance;
        int rangeY = 12;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int spawned = 0;
        for (int i = 0; i < attempts; i++) {
            if (random.nextDouble() > chance) {
                continue;
            }

            int x = center.getX() + random.nextInt(rangeXZ * 2) - rangeXZ;
            int y = center.getY() + random.nextInt(rangeY * 2) - rangeY;
            int z = center.getZ() + random.nextInt(rangeXZ * 2) - rangeXZ;
            pos.set(x, y, z);

            if (level.getBlockState(pos).is(BlockTags.LEAVES)) {
                double px = x + random.nextDouble();
                double py = y + random.nextDouble();
                double pz = z + random.nextDouble();
                double dx = WindState.windX + (random.nextDouble() - 0.5) * 0.02;
                double dy = -0.05 - random.nextDouble() * 0.05;
                double dz = WindState.windZ + (random.nextDouble() - 0.5) * 0.02;
                level.addParticle(FallingLeafParticle.TYPE, px, py, pz, dx, dy, dz);
                spawned++;
                if (spawned >= config.maxParticlesPerTick) {
                    break;
                }
            }
        }
    }
}
