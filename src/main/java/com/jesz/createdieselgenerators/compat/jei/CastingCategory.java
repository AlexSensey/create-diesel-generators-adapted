package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.molds.CastingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class CastingCategory extends CDGRecipeCategory<CastingRecipe> {

    private final AnimatedSpoutCastingStation spout = new AnimatedSpoutCastingStation();
    protected CastingCategory(CDGRecipeCategory.Info<CastingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {
        ItemStack stack = CDGItems.MOLD.asStack();
        stack.set(CDGDataComponents.MOLD_TYPE, recipe.moldType.getId());
        builder
                .addSlot(RecipeIngredientRole.RENDER_ONLY, 36, 11)
                .setStandardSlotBackground()
                .addItemStack(stack);

        for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            addFluidSlot(builder, 36, 51, fluidIngredient);

        }

        int i = 0;
        int size = recipe.getRollableResults().size();
        for (ProcessingOutput result : recipe.getRollableResults()) {
            int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
            int yPosition = -19 * (i / 2) + 51;

            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, xPosition, yPosition)
                    .setStandardSlotBackground()
                    .addItemStack(result.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(result));
            i++;
        }
    }

    @Override
    public void draw(CastingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        int vRows = (1 + recipe.getRollableResults().size()) / 2;

        if (vRows <= 2)
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32);
        SizedFluidIngredient ingredient = recipe.getFluidIngredients().get(0);
        List<net.neoforged.neoforge.fluids.FluidStack> fluids = ingredient.ingredient().fluids().stream()
                .map(holder -> new net.neoforged.neoforge.fluids.FluidStack(holder.value(), ingredient.amount()))
                .toList();
        spout.withFluids(fluids).draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    }

    @Override
    public int getWidth() {
        return getBackground().getWidth();
    }

    @Override
    public int getHeight() {
        return getBackground().getHeight();
    }
}
