package com.leafenhanced.mod.client.wind;

import com.leafenhanced.mod.config.LeafEnhancedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class WindState {
    public static float windX = 0.0f;
    public static float windZ = 0.0f;
    public static boolean isStorm = false;

    private static float targetWindX = 0.0f;
    private static float targetWindZ = 0.0f;
    private static int changeTimer = 0;
    private static int soundTimer = 0;

    public static void update(Minecraft minecraft, RandomSource random) {
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        LeafEnhancedConfig config = LeafEnhancedConfig.get();

        boolean raining = level.isRaining();
        isStorm = raining && config.stormGusts;

        float strength = isStorm ? config.baseWindStrength * 3.0f : config.baseWindStrength;

        if (changeTimer-- <= 0) {
            float targetScale = isStorm ? 2.5f : 1.0f;
            targetWindX = (random.nextFloat() - 0.5f) * 2.0f * strength * targetScale;
            targetWindZ = (random.nextFloat() - 0.5f) * 2.0f * strength * targetScale;
            changeTimer = 60 + random.nextInt(100);
        }

        windX += (targetWindX - windX) * 0.05f;
        windZ += (targetWindZ - windZ) * 0.05f;

        if (config.windSounds && --soundTimer <= 0) {
            float gustStrength = Math.abs(windX) + Math.abs(windZ);
            if (gustStrength > config.baseWindStrength * 1.5f) {
                playWindSound(minecraft, random);
                soundTimer = 200 + random.nextInt(400);
            } else {
                soundTimer = 40 + random.nextInt(60);
            }
        }
    }

    private static void playWindSound(Minecraft minecraft, RandomSource random) {
        if (minecraft.player == null) {
            return;
        }
        double x = minecraft.player.getX() + (random.nextDouble() - 0.5) * 16.0;
        double y = minecraft.player.getY() + (random.nextDouble() - 0.5) * 8.0;
        double z = minecraft.player.getZ() + (random.nextDouble() - 0.5) * 16.0;
        float volume = 0.3f + (isStorm ? 0.4f : 0.0f);
        float pitch = 0.8f + random.nextFloat() * 0.4f;
        minecraft.getSoundManager().play(new SimpleSoundInstance(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, volume, pitch, random, x, y, z));
    }
}
