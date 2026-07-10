package com.leafenhanced.mod.client.particle;

import com.leafenhanced.mod.client.wind.WindState;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class LeafParticleRenderer extends SingleQuadParticle {
    protected LeafParticleRenderer(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, RandomSource random) {
        super(level, x, y, z, dx, dy, dz, sprites.get(random));
        this.setSize(0.2f, 0.2f);
        this.setLifetime(60 + random.nextInt(40));
        this.setAlpha(0.8f);
        this.roll = random.nextFloat() * (float) Math.PI;
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        super.tick();
        this.xd += WindState.windX;
        this.zd += WindState.windZ;
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
