package com.leafenhanced.mod.client;

import com.leafenhanced.mod.LeafEnhancedMod;
import com.leafenhanced.mod.client.block.LayeredLeafLitterRenderer;
import com.leafenhanced.mod.client.particle.LeafParticleRenderer;
import com.leafenhanced.mod.client.wind.LeafSectionTracker;
import com.leafenhanced.mod.client.wind.WindState;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import com.leafenhanced.mod.particle.FallingLeafParticle;
import com.leafenhanced.mod.tag.ModBiomeTags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

public class LeafEnhancedModClient implements ClientModInitializer {
    private static int tickCounter = 0;
    private static ClientLevel lastLevel = null;

    @Override
    public void onInitializeClient() {
        ParticleProviderRegistry.getInstance().register(FallingLeafParticle.TYPE, new LeafParticleRenderer.Provider());
        BlockEntityRenderers.register(LeafEnhancedMod.LEAF_LITTER_BE, LayeredLeafLitterRenderer::new);
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        ClientLevel level = minecraft.level;
        if (level != lastLevel) {
            tickCounter = 0;
            lastLevel = level;
        }

        LocalPlayer player = minecraft.player;
        RandomSource random = level.getRandom();

        WindState.update(random);
        LeafSectionTracker.flush();

        LeafEnhancedConfig config = LeafEnhancedConfig.get();

        if (++tickCounter % config.spawnInterval != 0) {
            return;
        }

        BlockPos center = player.blockPosition();
        int rangeXZ = config.particleRenderDistance;
        int rangeY = 12;
        float maxDistSqr = rangeXZ * rangeXZ + rangeY * rangeY;

        double chance = config.fallingLeaves ? config.particleChance : 0;
        double falloff = config.distanceFalloff;
        boolean canSpawn = chance > 0 && config.maxParticlesPerTick > 0
            && LeafParticleRenderer.getActiveCount() < config.maxActiveParticles;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int attempts = Math.max(config.maxParticlesPerTick, 30);
        int spawned = 0;
        for (int i = 0; i < attempts; i++) {
            int x = center.getX() + random.nextInt(rangeXZ * 2) - rangeXZ;
            int y = center.getY() + 4 + random.nextInt(rangeY);
            int z = center.getZ() + random.nextInt(rangeXZ * 2) - rangeXZ;
            pos.set(x, y, z);

            BlockState leafState = level.getBlockState(pos);
            if (!leafState.is(BlockTags.LEAVES)) {
                continue;
            }

            LeafSectionTracker.mark(x, y, z);

            if (!canSpawn) {
                continue;
            }

            BlockPos belowPos = pos.below();
            if (!level.getBlockState(belowPos).isAir()) {
                continue;
            }

            float ddx = x - center.getX();
            float ddy = y - center.getY();
            float ddz = z - center.getZ();
            float distSqr = ddx * ddx + ddy * ddy + ddz * ddz;
            float distanceFactor = 1.0f - (float)(Math.min(1.0, distSqr / maxDistSqr) * falloff);
            distanceFactor = Math.max(distanceFactor, 0.2f);

            if (random.nextDouble() > chance * distanceFactor) {
                continue;
            }

            Holder<Biome> biome = level.getBiome(pos);
            if (!biome.is(ModBiomeTags.DECIDUOUS_BIOMES)) {
                continue;
            }

            double px = x + random.nextDouble();
            double py = y + random.nextDouble();
            double pz = z + random.nextDouble();
            double vx = (random.nextDouble() - 0.5) * 0.02;
            double vy = -0.05 - random.nextDouble() * 0.05;
            double vz = (random.nextDouble() - 0.5) * 0.02;
            level.addParticle(FallingLeafParticle.TYPE, px, py, pz, vx, vy, vz);
            spawned++;
            if (spawned >= config.maxParticlesPerTick) {
                break;
            }
        }
    }
}
