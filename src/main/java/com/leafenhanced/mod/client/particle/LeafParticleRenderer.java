package com.leafenhanced.mod.client.particle;

import com.leafenhanced.mod.client.wind.WindState;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class LeafParticleRenderer extends SingleQuadParticle {
    private static int activeCount = 0;

    public static int getActiveCount() {
        return activeCount;
    }

    protected LeafParticleRenderer(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, dx, dy, dz, sprites.get(random));
        activeCount++;
        this.setSize(0.2f, 0.2f);
        LeafEnhancedConfig config = LeafEnhancedConfig.get();
        this.setLifetime(config.particleLifetime + random.nextInt(20) - 10);
        this.setAlpha(0.8f);
        this.roll = random.nextFloat() * (float) Math.PI;
        this.oRoll = this.roll;
    }

    @Override
    public void remove() {
        super.remove();
        activeCount--;
    }

    @Override
    public void tick() {
        super.tick();
        float maxWind = LeafEnhancedConfig.get().particleMaxWindSpeed;
        this.xd = Mth.clamp(this.xd + WindState.windX, -maxWind, maxWind);
        this.zd = Mth.clamp(this.zd + WindState.windZ, -maxWind, maxWind);
        this.roll += (this.random.nextFloat() - 0.5f) * 0.1f;
        if (this.age > this.lifetime - 20) {
            this.setAlpha(0.8f * (1.0f - (float) (this.age - (this.lifetime - 20)) / 20.0f));
        }
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProviderRegistry.PendingParticleProvider<SimpleParticleType> {
        @Override
        public ParticleProvider<SimpleParticleType> create(FabricSpriteSet fabricSpriteSet) {
            return (type, level, x, y, z, dx, dy, dz, random) ->
                    new LeafParticleRenderer(level, x, y, z, dx, dy, dz, fabricSpriteSet, random);
        }
    }
}
