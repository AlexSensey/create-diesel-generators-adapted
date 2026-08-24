package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.tools.hammer.HammerRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class HammeringCategory extends CDGRecipeCategory<HammerRecipe> {
    final ItemStack renderedHammer = CDGItems.HAMMER.asStack();
    public HammeringCategory(CDGRecipeCategory.Info<HammerRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HammerRecipe recipe, IFocusGroup iFocusGroup) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 27, 29)
                .setStandardSlotBackground()
                .addIngredients(recipe.getIngredients().get(0));

        ProcessingOutput output = recipe.getRollableResults().get(0);
        builder
                .addSlot(RecipeIngredientRole.OUTPUT, 132, 29)
                .setStandardSlotBackground()
                .addItemStack(output.getStack())
                .addRichTooltipCallback(addStochasticTooltip(output));
    }

    @Override
    public void draw(HammerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);

        java.util.List<ItemStack> matchingStacks = recipe.getIngredients().get(0).display()
                .resolveForStacks(SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
        if (matchingStacks.isEmpty())
            return;

        renderedHammer.set(CDGDataComponents.PROCESSING_ITEM, new SandPaperItemComponent(matchingStacks.getFirst()));
        GuiGameElement.of(renderedHammer)
                .at(getBackground().getWidth() / 2 - 16, 0, 0)
                .scale(2)
                .submit(graphics);
    }

    @Override public int getWidth() { return getBackground().getWidth(); }
    @Override public int getHeight() { return getBackground().getHeight(); }
}
