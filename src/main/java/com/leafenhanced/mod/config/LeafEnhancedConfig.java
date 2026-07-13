package com.leafenhanced.mod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leafenhanced.mod.LeafEnhancedMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LeafEnhancedConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(LeafEnhancedMod.MOD_ID + ".json");

    public boolean fallingLeaves = true;
    public double particleChance = 0.1;
    public int maxParticlesPerTick = 50;
    public int particleRenderDistance = 32;
    public double distanceFalloff = 0.6;
    public int maxActiveParticles = 1000;
    public int particleLifetime = 80;
    public int spawnInterval = 5;
    public float baseWindStrength = 0.02f;
    public float particleMaxWindSpeed = 0.3f;
    public float leafSwayStrength = 0.02f;

    public boolean leafLitterAccumulation = true;
    public double accumulationChance = 0.05;
    public boolean leafLitterDisappearance = true;
    public int maxLayers = 4;
    public int accumulationSpeed = 100;
    public int disappearanceTicks = 36000;
    public int disappearanceSpeed = 100;

    public boolean leafLitterShovelCollect = true;

    private static LeafEnhancedConfig INSTANCE = new LeafEnhancedConfig();

    public static LeafEnhancedConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH);
            LeafEnhancedConfig loaded = GSON.fromJson(json, LeafEnhancedConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
                INSTANCE.validate();
            }
        } catch (IOException e) {
            LeafEnhancedMod.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            LeafEnhancedMod.LOGGER.error("Failed to save config", e);
        }
    }

    public void validate() {
        particleChance = clamp(particleChance, 0.0, 1.0);
        maxParticlesPerTick = clamp(maxParticlesPerTick, 0, 500);
        particleRenderDistance = clamp(particleRenderDistance, 4, 64);
        distanceFalloff = clamp(distanceFalloff, 0.0, 1.0);
        maxActiveParticles = clamp(maxActiveParticles, 100, 5000);
        particleLifetime = clamp(particleLifetime, 20, 200);
        spawnInterval = clamp(spawnInterval, 1, 20);
        accumulationSpeed = clamp(accumulationSpeed, 0, 500);
        disappearanceSpeed = clamp(disappearanceSpeed, 0, 500);
        baseWindStrength = clamp(baseWindStrength, 0.0f, 1.0f);
        particleMaxWindSpeed = clamp(particleMaxWindSpeed, 0.05f, 1.0f);
        leafSwayStrength = clamp(leafSwayStrength, 0.0f, 1.0f);
        accumulationChance = clamp(accumulationChance, 0.0, 1.0);
        maxLayers = clamp(maxLayers, 1, 4);
        disappearanceTicks = clamp(disappearanceTicks, 20, 1200000);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
