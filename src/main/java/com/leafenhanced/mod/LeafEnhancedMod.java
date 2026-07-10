package com.leafenhanced.mod;

import com.leafenhanced.mod.block.LayeredLeafLitterBlock;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import com.leafenhanced.mod.particle.FallingLeafParticle;
import com.leafenhanced.mod.world.LeafLitterHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafEnhancedMod implements ModInitializer {
    public static final String MOD_ID = "leafenhanced";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Block LAYERED_LEAF_LITTER;
    public static Item LAYERED_LEAF_LITTER_ITEM;

    public static GameRule<Integer> LEAVES_ACCUMULATION_SPEED;
    public static GameRule<Integer> LEAVES_PARTICLE_DENSITY;
    public static GameRule<Integer> LEAVES_DISAPPEARANCE_SPEED;

    @Override
    public void onInitialize() {
        LeafEnhancedConfig.load();
        registerBlocks();
        registerParticles();
        registerGameRules();
        LeafLitterHandler.init();

        LOGGER.info("LeafEnhanced initialized");
    }


    private void registerBlocks() {
        LAYERED_LEAF_LITTER = Registry.register(
                BuiltInRegistries.BLOCK,
                Identifier.fromNamespaceAndPath(MOD_ID, "layered_leaf_litter"),
                new LayeredLeafLitterBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.PLANT)
                        .sound(SoundType.GRASS)
                        .strength(0.1f)
                        .noCollision()
                        .noOcclusion()
                        .ignitedByLava()
                        .replaceable())
        );

        LAYERED_LEAF_LITTER_ITEM = Registry.register(
                BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(MOD_ID, "layered_leaf_litter"),
                new BlockItem(LAYERED_LEAF_LITTER, new Item.Properties())
        );
    }

    private void registerGameRules() {
        LEAVES_ACCUMULATION_SPEED = GameRuleBuilder.forInteger(100)
                .category(GameRuleCategory.UPDATES)
                .buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "leaves_accumulation_speed"));
        LEAVES_PARTICLE_DENSITY = GameRuleBuilder.forInteger(100)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "leaves_particle_density"));
        LEAVES_DISAPPEARANCE_SPEED = GameRuleBuilder.forInteger(100)
                .category(GameRuleCategory.UPDATES)
                .buildAndRegister(Identifier.fromNamespaceAndPath(MOD_ID, "leaves_disappearance_speed"));
    }

    private void registerParticles() {
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "falling_leaf"),
                FallingLeafParticle.TYPE
        );
    }
}
