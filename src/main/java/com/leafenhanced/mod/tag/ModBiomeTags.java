package com.leafenhanced.mod.tag;

import com.leafenhanced.mod.LeafEnhancedMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomeTags {
    public static final TagKey<Biome> DECIDUOUS_BIOMES = TagKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(LeafEnhancedMod.MOD_ID, "deciduous_biomes")
    );
}
