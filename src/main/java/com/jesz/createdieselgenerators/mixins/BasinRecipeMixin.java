package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.molds.CompressionMoldingRecipe;
import com.jesz.createdieselgenerators.content.molds.MoldItem;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BasinRecipe.class)
public class BasinRecipeMixin {
    @Inject(method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z", at=@At("HEAD"), remap = false, cancellable = true)
    private static void apply(BasinBlockEntity basin, Recipe<?> recipe, boolean test, CallbackInfoReturnable<Boolean> cir){
        if (recipe instanceof CompressionMoldingRecipe moldingRecipe) {
            IItemHandler availableItems = basin.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);
            if (availableItems == null) {
                cir.setReturnValue(false);
                return;
            }
            ItemStack moldStack = null;
            for (int i = 0; i < availableItems.getSlots(); i++) {
                ItemStack stack = availableItems.getStackInSlot(i);
                if (CDGItems.MOLD.isIn(stack))
                    moldStack = stack;
            }
            if (moldStack == null){
                cir.setReturnValue(false);
                return;
            }
            MoldType type = MoldItem.getMold(moldStack);
            if (moldingRecipe.moldType != type)
                cir.setReturnValue(false);
        }
    }
}
