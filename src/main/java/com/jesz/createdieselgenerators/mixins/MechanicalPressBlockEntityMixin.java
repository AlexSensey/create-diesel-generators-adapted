package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MechanicalPressBlockEntity.class)
public class MechanicalPressBlockEntityMixin {
    @Inject(method = "matchStaticFilters", at = @At("RETURN"), remap = false, cancellable = true)
    public void matchStaticFilters(RecipeHolder<? extends Recipe<?>> recipe, CallbackInfoReturnable<Boolean> cir){
        if (cir.getReturnValue())
            return;
        if (recipe.value().getType() == CDGRecipes.COMPRESSION_MOLDING.getType())
            cir.setReturnValue(true);
    }
}
