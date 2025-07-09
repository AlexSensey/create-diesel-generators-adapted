package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaftBlock.class)
public class ShaftBlockMixin {

    @Inject(method = "pickCorrectShaftType", at = @At("HEAD"), remap = false, cancellable = true)
    private static void pickCorrectShaftType(BlockState stateForPlacement, Level level, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (PoweredEngineShaftBlock.stillValid(stateForPlacement, level, pos))
            cir.setReturnValue(PoweredEngineShaftBlock.getEquivalent(stateForPlacement));
    }
}
