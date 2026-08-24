package com.jesz.createdieselgenerators.compat.jei;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.function.Supplier;

/** JEI 30 category base kept inside the addon so it does not depend on Create's internal JEI classes. */
public abstract class CDGRecipeCategory<T extends Recipe<?>> implements IRecipeCategory<RecipeHolder<T>> {
    private final IRecipeType<RecipeHolder<T>> type;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final Supplier<List<RecipeHolder<T>>> recipes;
    private final List<Supplier<? extends ItemStack>> catalysts;

    protected CDGRecipeCategory(Info<T> info) {
        type = info.recipeType();
        title = info.title();
        background = info.background();
        icon = info.icon();
        recipes = info.recipes();
        catalysts = info.catalysts();
    }

    @Override public IRecipeType<RecipeHolder<T>> getRecipeType() { return type; }
    @Override public Component getTitle() { return title; }
    @Override public int getWidth() { return background.getWidth(); }
    @Override public int getHeight() { return background.getHeight(); }
    @Override public IDrawable getIcon() { return icon; }
    public IDrawable getBackground() { return background; }

    @Override
    public final void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> holder, IFocusGroup focuses) {
        setRecipe(builder, holder.value(), focuses);
    }

    @Override
    public final void draw(RecipeHolder<T> holder, IRecipeSlotsView slots, GuiGraphicsExtractor graphics,
                           double mouseX, double mouseY) {
        draw(holder.value(), slots, graphics, mouseX, mouseY);
    }

    @Override public Identifier getIdentifier(RecipeHolder<T> holder) { return holder.id().identifier(); }

    protected abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);
    protected abstract void draw(T recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics,
                                 double mouseX, double mouseY);

    public void registerRecipes(IRecipeRegistration registration) { registration.addRecipes(type, recipes.get()); }
    public void registerCatalysts(IRecipeCatalystRegistration registration) {
        catalysts.forEach(stack -> registration.addRecipeCatalyst(stack.get(), type));
    }

    protected static IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y) {
        return builder.addSlot(role, x, y).setStandardSlotBackground();
    }

    protected static IRecipeSlotBuilder addFluidSlot(IRecipeLayoutBuilder builder, int x, int y,
                                                       SizedFluidIngredient ingredient) {
        List<FluidStack> fluids = ingredient.ingredient().fluids().stream()
                .map(holder -> new FluidStack(holder.value(), ingredient.amount())).toList();
        return addSlot(builder, RecipeIngredientRole.INPUT, x, y)
                .addIngredients(NeoForgeTypes.FLUID_STACK, fluids)
                .setFluidRenderer(ingredient.amount(), false, 16, 16);
    }

    protected static IRecipeSlotBuilder addFluidSlot(IRecipeLayoutBuilder builder, int x, int y, FluidStack fluid) {
        return addSlot(builder, RecipeIngredientRole.OUTPUT, x, y)
                .addIngredient(NeoForgeTypes.FLUID_STACK, fluid)
                .setFluidRenderer(fluid.getAmount(), false, 16, 16);
    }

    protected static IRecipeSlotRichTooltipCallback addStochasticTooltip(ProcessingOutput output) {
        return (view, tooltip) -> {
            float chance = output.getChance();
            if (chance != 1)
                tooltip.add(Component.translatable("create.recipe.processing.chance",
                        chance < .01f ? "<1" : (int) (chance * 100)).withStyle(ChatFormatting.GOLD));
        };
    }

    public record Info<T extends Recipe<?>>(IRecipeType<RecipeHolder<T>> recipeType, Component title,
                                             IDrawable background, IDrawable icon,
                                             Supplier<List<RecipeHolder<T>>> recipes,
                                             List<Supplier<? extends ItemStack>> catalysts) {}

    @FunctionalInterface
    public interface Factory<T extends Recipe<?>> {
        CDGRecipeCategory<T> create(Info<T> info);
    }
}
