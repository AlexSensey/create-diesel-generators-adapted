package com.jesz.createdieselgenerators.events.datagen;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.compat.strut_your_stuff.StrutYourStuffRegistryEntries;
import com.simibubi.create.AllItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.concurrent.CompletableFuture;

public class CDGRecipeProvider extends RecipeProvider {
    public CDGRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);

    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StrutYourStuffRegistryEntries.ANDESITE_GIRDER_STRUT, 6)
                .pattern("A  ")
                .pattern("GGG")
                .pattern("  A")
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('G', CDGBlocks.ANDESITE_GIRDER)
                .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
                .save(recipeOutput.withConditions(new ModLoadedCondition("struts")), CreateDieselGenerators.rl("crafting/andesite_girder_strut"));

    }
}
