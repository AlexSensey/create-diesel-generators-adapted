package com.jesz.createdieselgenerators.content.tools.hammer;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class HammerRecipe extends StandardProcessingRecipe<HammerRecipe.HammerInv> {
    public HammerRecipe(ProcessingRecipeParams params) {
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
    public boolean matches(HammerInv input, Level level) {
        return ingredients.get(0).test(input.getItem(0));
    }

    public static class HammerInv extends RecipeWrapper {
        public HammerInv(ItemStack stack) {
            super(new ItemStackHandler(1));
            inv.insertItem(0, stack, false);
        }
    }
}
