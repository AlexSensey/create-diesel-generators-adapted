package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.molds.CastingRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

public class CastingCategory extends CreateRecipeCategory<CastingRecipe> {

    private final AnimatedSpoutCastingStation spout = new AnimatedSpoutCastingStation();
    protected CastingCategory(Info<CastingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {
        ItemStack stack = CDGItems.MOLD.asStack();
        stack.addTagElement("Mold", StringTag.valueOf(recipe.moldType.getId().toString()));
        builder
                .addSlot(RecipeIngredientRole.CATALYST, 36, 11)
                .setBackground(getRenderedSlot(), -1, -1)
                .addItemStack(stack);

        for (FluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
            builder
                    .addSlot(RecipeIngredientRole.INPUT, 36, 51)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(ForgeTypes.FLUID_STACK, withImprovedVisibility(fluidIngredient.getMatchingFluidStacks()))
                    .addRichTooltipCallback(addFluidTooltip(fluidIngredient.getRequiredAmount()));

        }

        int i = 0;
        int size = recipe.getRollableResults().size();
        for (ProcessingOutput result : recipe.getRollableResults()) {
            int xPosition = 142 - (size % 2 != 0 && i == size - 1 ? 0 : i % 2 == 0 ? 10 : -9);
            int yPosition = -19 * (i / 2) + 51;

            builder
                    .addSlot(RecipeIngredientRole.OUTPUT, xPosition, yPosition)
                    .setBackground(getRenderedSlot(result), -1, -1)
                    .addItemStack(result.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(result));
            i++;
        }
    }

    @Override
    public void draw(CastingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, iRecipeSlotsView, graphics, mouseX, mouseY);
        AllGuiTextures.JEI_SHADOW.render(graphics, 81, 68);
        int vRows = (1 + recipe.getRollableResults().size()) / 2;

        if (vRows <= 2)
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 136, -19 * (vRows - 1) + 32);
        spout.withFluids(recipe.getFluidIngredients().get(0).getMatchingFluidStacks()).draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    }
}
