package com.leafenhanced.mod.mixin;

import com.leafenhanced.mod.LeafEnhancedMod;
import com.leafenhanced.mod.world.LeafLitterHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        try {
            LeafLitterHandler.onLeafRandomTick(state, level, pos, random);
        } catch (Exception e) {
            LeafEnhancedMod.LOGGER.warn("Failed to handle leaf random tick", e);
        }
    }
}
