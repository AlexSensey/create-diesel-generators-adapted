package com.jesz.createdieselgenerators.content.tools.wire_cutters;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class WireCuttingRecipe extends ProcessingRecipe<WireCuttingRecipe.WireCuttingInv> {
    public WireCuttingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDGRecipes.WIRE_CUTTING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(WireCuttingInv inv, Level level) {
        return ingredients.get(0).test(inv.getItem(0));
    }

    public static class WireCuttingInv extends RecipeWrapper {
        public WireCuttingInv(ItemStack stack) {
            super(new ItemStackHandler(1));
            inv.setStackInSlot(0, stack);
        }
    }
}
