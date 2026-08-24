package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.molds.CompressionMoldingRecipe;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class CompressionMoldingCategory extends CDGRecipeCategory<BasinRecipe> {
    private final AnimatedPress press = new AnimatedPress(true);
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    protected CompressionMoldingCategory(CDGRecipeCategory.Info<BasinRecipe> info) {
        super(info);
    }

    @Override
    protected void setRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses) {
        int input = 0;
        int inputCount = recipe.getIngredients().size() + recipe.getFluidIngredients().size();
        int offset = inputCount < 3 ? (3 - inputCount) * 19 / 2 : 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            addSlot(builder, RecipeIngredientRole.INPUT, 17 + offset + input % 3 * 19,
                    51 - input / 3 * 19).add(ingredient);
            input++;
        }
        for (SizedFluidIngredient ingredient : recipe.getFluidIngredients()) {
            addFluidSlot(builder, 17 + offset + input % 3 * 19, 51 - input / 3 * 19, ingredient);
            input++;
        }

        int output = 0;
        int outputCount = recipe.getRollableResults().size() + recipe.getFluidResults().size();
        for (ProcessingOutput result : recipe.getRollableResults()) {
            int x = 142 - (outputCount % 2 != 0 && output == outputCount - 1 ? 0 : output % 2 == 0 ? 10 : -9);
            int y = 51 - 19 * (output / 2);
            addSlot(builder, RecipeIngredientRole.OUTPUT, x, y).addItemStack(result.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(result));
            output++;
        }
        for (FluidStack result : recipe.getFluidResults()) {
            int x = 142 - (outputCount % 2 != 0 && output == outputCount - 1 ? 0 : output % 2 == 0 ? 10 : -9);
            int y = 51 - 19 * (output / 2);
            addFluidSlot(builder, x, y, result);
            output++;
        }

        if (recipe instanceof CompressionMoldingRecipe molding) {
            ItemStack mold = CDGItems.MOLD.asStack();
            mold.set(CDGDataComponents.MOLD_TYPE, molding.moldType.getId());
            addSlot(builder, RecipeIngredientRole.RENDER_ONLY, 36, 11).addItemStack(mold);
        }

        HeatCondition heat = recipe.getRequiredHeat();
        if (!heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE))
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 81).addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        if (!heat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED))
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 153, 81).addItemStack(AllItems.BLAZE_CAKE.asStack());
    }

    @Override
    protected void draw(BasinRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics,
                        double mouseX, double mouseY) {
        int rows = (1 + recipe.getRollableResults().size() + recipe.getFluidResults().size()) / 2;
        if (rows <= 2)
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (rows - 1) + 32);
        HeatCondition heat = recipe.getRequiredHeat();
        AllGuiTextures shadow = heat == HeatCondition.NONE ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
        shadow.render(graphics, 81, 58 + (heat == HeatCondition.NONE ? 10 : 30));
        if (heat != HeatCondition.NONE)
            heater.withHeat(heat.visualizeAsBlazeBurner()).draw(graphics, getWidth() / 2 + 3, 55);
        press.draw(graphics, getWidth() / 2 + 3, 34);
    }
}
