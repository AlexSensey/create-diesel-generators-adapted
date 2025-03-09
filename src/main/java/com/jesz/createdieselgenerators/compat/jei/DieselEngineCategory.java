package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.*;

@ParametersAreNonnullByDefault
public class DieselEngineCategory implements IRecipeCategory<DieselEngineJeiRecipeType> {
    IGuiHelper guiHelper;
    AnimatedDieselEngineElement engine = new AnimatedDieselEngineElement();
    public DieselEngineCategory(IGuiHelper helper) {
        this.guiHelper = helper;

    }

    @Override
    public RecipeType<DieselEngineJeiRecipeType> getRecipeType() {
        return DieselEngineJeiRecipeType.DIESEL_COMBUSTION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("createdieselgenerators.recipe.diesel_combustion");
    }

    @Override
    public IDrawable getBackground() {
        return new EmptyBackground(177,70);
    }

    @Override
    public IDrawable getIcon() {
        return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, CDGBlocks.DIESEL_ENGINE.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DieselEngineJeiRecipeType recipe, IFocusGroup iFocusGroup) {
        addFluidSlot(builder, 10, 10, new FluidStack(recipe.fluid, 1000));
    }

    @Override
    public void draw(DieselEngineJeiRecipeType recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_ARROW.render(graphics, 82, 40);
        AllGuiTextures.JEI_SHADOW.render(graphics, 28, 52);
        byte enginesEnabled = (byte) ((DieselEngineBlock.EngineTypes.NORMAL.enabled() ? 1 : 0) + (DieselEngineBlock.EngineTypes.MODULAR.enabled() ? 1 : 0) + (DieselEngineBlock.EngineTypes.HUGE.enabled() ? 1 : 0));
        int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
        List<DieselEngineBlock.EngineTypes> enabledEngines = Arrays.stream(DieselEngineBlock.EngineTypes.values()).filter(DieselEngineBlock.EngineTypes::enabled).toList();
        DieselEngineBlock.EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
        float currentSpeed = FuelTypeManager.getGeneratedSpeed(currentEngine, recipe.fluid);
        float currentCapacity = FuelTypeManager.getGeneratedStress(currentEngine, recipe.fluid);
        float currentBurn = FuelTypeManager.getBurnRate(currentEngine, recipe.fluid);

        graphics.drawString(Minecraft.getInstance().font, CreateLang.number(currentBurn).component().append(Component.translatable("createdieselgenerators.generic.unit.mbps")), 5,
                40, 0x888888, false);
        graphics.drawString(Minecraft.getInstance().font, CreateLang.number(currentCapacity/currentSpeed).component().append("x").append(Component.translatable("create.generic.unit.rpm")), 125,
                41, 0x888888, false);
        graphics.drawString(Minecraft.getInstance().font, CreateLang.number(currentSpeed).component().append(Component.translatable("create.generic.unit.rpm")), 85,
                33, 0x888888, false);
        graphics.drawString(Minecraft.getInstance().font, CreateLang.number(currentCapacity).component().append(Component.translatable("create.generic.unit.stress")), 81,
                50, 0x888888, false);
        engine.draw(graphics, 47, 62);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 40, 15);

    }
}