package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
    protected CreeperMixin(EntityType<? extends Monster> type, Level level) { super(type, level); }

    @Shadow public abstract void ignite();

    @Inject(method = "mobInteract", at = @At("TAIL"))
    public void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        ItemStack stackInHand = player.getItemInHand(hand);
        if(!CDGItems.LIGHTER.isIn(stackInHand))
            return;
        IFluidHandlerItem fluid = stackInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).orElse(new FluidHandlerItemStack(stackInHand, 0));
        if(fluid.getFluidInTank(0).isEmpty())
            return;
        fluid.drain(1, IFluidHandler.FluidAction.EXECUTE);
        ignite();
        this.level().playSound(player, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);


    }
}
