package com.leafenhanced.mod.client.wind;

import com.leafenhanced.mod.config.LeafEnhancedConfig;
import net.minecraft.util.RandomSource;

public class WindState {
    public static float windX = 0.0f;
    public static float windZ = 0.0f;

    private static float targetWindX = 0.0f;
    private static float targetWindZ = 0.0f;
    private static int changeTimer = 0;

    public static void update(RandomSource random) {
        LeafEnhancedConfig config = LeafEnhancedConfig.get();

        if (changeTimer-- <= 0) {
            targetWindX = (random.nextFloat() - 0.5f) * 2.0f * config.baseWindStrength;
            targetWindZ = (random.nextFloat() - 0.5f) * 2.0f * config.baseWindStrength;
            changeTimer = 200 + random.nextInt(400);
        }

        windX += (targetWindX - windX) * 0.05f;
        windZ += (targetWindZ - windZ) * 0.05f;
    }
}
