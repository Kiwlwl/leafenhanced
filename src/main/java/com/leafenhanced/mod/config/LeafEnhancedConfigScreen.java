package com.leafenhanced.mod.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LeafEnhancedConfigScreen {
    public static Screen create(Screen parent) {
        LeafEnhancedConfig config = LeafEnhancedConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.leafenhanced.config"));

        builder.setSavingRunnable(LeafEnhancedConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory leaves = builder.getOrCreateCategory(Component.translatable("category.leafenhanced.leaves"));
        leaves.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.leafenhanced.fallingLeaves"), config.fallingLeaves)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.leafenhanced.fallingLeaves.tooltip"))
                .setSaveConsumer(v -> config.fallingLeaves = v)
                .build());
        leaves.addEntry(entryBuilder.startDoubleField(Component.translatable("option.leafenhanced.particleChance"), config.particleChance)
                .setDefaultValue(0.1)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("option.leafenhanced.particleChance.tooltip"))
                .setSaveConsumer(v -> config.particleChance = v)
                .build());
        leaves.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.maxParticlesPerTick"), config.maxParticlesPerTick)
                .setDefaultValue(50)
                .setMin(0)
                .setMax(500)
                .setTooltip(Component.translatable("option.leafenhanced.maxParticlesPerTick.tooltip"))
                .setSaveConsumer(v -> config.maxParticlesPerTick = v)
                .build());
        leaves.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.particleRenderDistance"), config.particleRenderDistance)
                .setDefaultValue(32)
                .setMin(4)
                .setMax(64)
                .setTooltip(Component.translatable("option.leafenhanced.particleRenderDistance.tooltip"))
                .setSaveConsumer(v -> config.particleRenderDistance = v)
                .build());
        leaves.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.spawnInterval"), config.spawnInterval)
                .setDefaultValue(5)
                .setMin(1)
                .setMax(20)
                .setTooltip(Component.translatable("option.leafenhanced.spawnInterval.tooltip"))
                .setSaveConsumer(v -> config.spawnInterval = v)
                .build());
        leaves.addEntry(entryBuilder.startFloatField(Component.translatable("option.leafenhanced.baseWindStrength"), config.baseWindStrength)
                .setDefaultValue(0.02f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Component.translatable("option.leafenhanced.baseWindStrength.tooltip"))
                .setSaveConsumer(v -> config.baseWindStrength = v)
                .build());
        leaves.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.accumulationSpeed"), config.accumulationSpeed)
                .setDefaultValue(100)
                .setMin(0)
                .setMax(500)
                .setTooltip(Component.translatable("option.leafenhanced.accumulationSpeed.tooltip"))
                .setSaveConsumer(v -> config.accumulationSpeed = v)
                .build());
        leaves.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.disappearanceSpeed"), config.disappearanceSpeed)
                .setDefaultValue(100)
                .setMin(0)
                .setMax(500)
                .setTooltip(Component.translatable("option.leafenhanced.disappearanceSpeed.tooltip"))
                .setSaveConsumer(v -> config.disappearanceSpeed = v)
                .build());

        ConfigCategory perf = builder.getOrCreateCategory(Component.translatable("category.leafenhanced.performance"));
        perf.addEntry(entryBuilder.startDoubleField(Component.translatable("option.leafenhanced.distanceFalloff"), config.distanceFalloff)
                .setDefaultValue(0.6)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("option.leafenhanced.distanceFalloff.tooltip"))
                .setSaveConsumer(v -> config.distanceFalloff = v)
                .build());
        perf.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.maxActiveParticles"), config.maxActiveParticles)
                .setDefaultValue(1000)
                .setMin(100)
                .setMax(5000)
                .setTooltip(Component.translatable("option.leafenhanced.maxActiveParticles.tooltip"))
                .setSaveConsumer(v -> config.maxActiveParticles = v)
                .build());
        perf.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.particleLifetime"), config.particleLifetime)
                .setDefaultValue(80)
                .setMin(20)
                .setMax(200)
                .setTooltip(Component.translatable("option.leafenhanced.particleLifetime.tooltip"))
                .setSaveConsumer(v -> config.particleLifetime = v)
                .build());
        perf.addEntry(entryBuilder.startFloatField(Component.translatable("option.leafenhanced.particleMaxWindSpeed"), config.particleMaxWindSpeed)
                .setDefaultValue(0.3f)
                .setMin(0.05f)
                .setMax(1.0f)
                .setTooltip(Component.translatable("option.leafenhanced.particleMaxWindSpeed.tooltip"))
                .setSaveConsumer(v -> config.particleMaxWindSpeed = v)
                .build());

        ConfigCategory litter = builder.getOrCreateCategory(Component.translatable("category.leafenhanced.litter"));
        litter.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.leafenhanced.leafLitterAccumulation"), config.leafLitterAccumulation)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.leafenhanced.leafLitterAccumulation.tooltip"))
                .setSaveConsumer(v -> config.leafLitterAccumulation = v)
                .build());
        litter.addEntry(entryBuilder.startDoubleField(Component.translatable("option.leafenhanced.accumulationChance"), config.accumulationChance)
                .setDefaultValue(0.05)
                .setMin(0.0)
                .setMax(1.0)
                .setTooltip(Component.translatable("option.leafenhanced.accumulationChance.tooltip"))
                .setSaveConsumer(v -> config.accumulationChance = v)
                .build());
        litter.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.leafenhanced.leafLitterDisappearance"), config.leafLitterDisappearance)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.leafenhanced.leafLitterDisappearance.tooltip"))
                .setSaveConsumer(v -> config.leafLitterDisappearance = v)
                .build());
        litter.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.maxLayers"), config.maxLayers)
                .setDefaultValue(4)
                .setMin(1)
                .setMax(4)
                .setTooltip(Component.translatable("option.leafenhanced.maxLayers.tooltip"))
                .setSaveConsumer(v -> config.maxLayers = v)
                .build());
        litter.addEntry(entryBuilder.startIntField(Component.translatable("option.leafenhanced.disappearanceTicks"), config.disappearanceTicks)
                .setDefaultValue(36000)
                .setMin(20)
                .setMax(1200000)
                .setTooltip(Component.translatable("option.leafenhanced.disappearanceTicks.tooltip"))
                .setSaveConsumer(v -> config.disappearanceTicks = v)
                .build());
        litter.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.leafenhanced.leafLitterShovelCollect"), config.leafLitterShovelCollect)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("option.leafenhanced.leafLitterShovelCollect.tooltip"))
                .setSaveConsumer(v -> config.leafLitterShovelCollect = v)
                .build());

        return builder.build();
    }
}
