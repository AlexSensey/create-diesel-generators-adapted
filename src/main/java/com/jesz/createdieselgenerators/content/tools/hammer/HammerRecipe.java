package com.jesz.createdieselgenerators.content.tools.hammer;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class HammerRecipe extends ProcessingRecipe<HammerRecipe.HammerInv> {
    public HammerRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDGRecipes.HAMMERING, params);
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
    public boolean matches(HammerInv inv, Level level) {
        return ingredients.get(0).test(inv.getItem(0));
    }

    public static class HammerInv extends RecipeWrapper {
        public HammerInv(ItemStack stack) {
            super(new ItemStackHandler(1));
            inv.setStackInSlot(0, stack);
        }
    }
}
