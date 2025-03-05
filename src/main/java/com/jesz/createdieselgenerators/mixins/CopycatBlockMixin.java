package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CopycatBlock.class)
public class CopycatBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if(stackInHand.getItem() instanceof DyeItem di){
            if(level.getBlockEntity(pos) instanceof CopycatBlockEntity be){
                if(CDGBlocks.OIL_BARREL.has(be.getMaterial()))
                    be.setMaterial(be.getMaterial().setValue(OilBarrelBlock.OIL_BARREL_COLOR, OilBarrelBlock.OilBarrelColor.getForDyeColor(di.getDyeColor())));
            }
            if(!player.isCreative())
                stackInHand.shrink(1);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

}
