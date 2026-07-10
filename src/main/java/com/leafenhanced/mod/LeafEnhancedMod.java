package com.leafenhanced.mod;

import com.leafenhanced.mod.block.LayeredLeafLitterBlock;
import com.leafenhanced.mod.block.LayeredLeafLitterBlockEntity;
import com.leafenhanced.mod.config.LeafEnhancedConfig;
import com.leafenhanced.mod.particle.FallingLeafParticle;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafEnhancedMod implements ModInitializer {
    public static final String MOD_ID = "leafenhanced";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Block LAYERED_LEAF_LITTER;
    public static Item LAYERED_LEAF_LITTER_ITEM;
    public static BlockEntityType<LayeredLeafLitterBlockEntity> LEAF_LITTER_BE;

    @Override
    public void onInitialize() {
        LeafEnhancedConfig.load();
        registerBlocks();
        registerBlockEntities();
        registerParticles();

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

    private void registerBlockEntities() {
        LEAF_LITTER_BE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "layered_leaf_litter"),
                new BlockEntityType<>(LayeredLeafLitterBlockEntity::new, LAYERED_LEAF_LITTER)
        );
    }

    private void registerParticles() {
        Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(MOD_ID, "falling_leaf"),
                FallingLeafParticle.TYPE
        );
    }
}
