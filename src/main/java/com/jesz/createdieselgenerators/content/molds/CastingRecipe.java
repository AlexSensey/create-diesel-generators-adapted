package com.jesz.createdieselgenerators.content.molds;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class CastingRecipe extends StandardProcessingRecipe<RecipeInput> {
    public MoldType moldType;
    MoldRecipeParams params;

    public CastingRecipe(MoldRecipeParams params) {
        super(CDGRecipes.CASTING, params);
        this.moldType = MoldType.findById(params.mold());
        this.params = params;
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    public MoldRecipeParams getParams() {
        return params;
    }

    public boolean matches(BasinBlockEntity basin, FluidStack fluidStack) {
        if (moldType == null)
            return false;
        if (getFluidIngredients().size() != 1)
            return false;

        IItemHandler availableItems = basin.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);

        if (availableItems == null)
            return false;

        MoldType moldInBasin = null;
        for (int i = 0; i < availableItems.getSlots(); i++) {
            ItemStack stack = availableItems.getStackInSlot(i);

            if (stack.getItem() instanceof MoldItem && MoldItem.getMold(stack) == moldType)
                moldInBasin = MoldItem.getMold(stack);
        }

        if (moldInBasin == null)
            return false;


        if (getFluidIngredients().get(0).test(fluidStack))
            return true;

        return false;
    }

    public int execute(BasinBlockEntity basin, boolean simulate) {
        IItemHandler availableItems = basin.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);

        if (availableItems == null)
            return 0;

        MoldType moldInBasin = null;
        for (int i = 0; i < availableItems.getSlots(); i++) {
            ItemStack stack = availableItems.getStackInSlot(i);

            if (stack.getItem() instanceof MoldItem && MoldItem.getMold(stack) == moldType)
                moldInBasin = MoldItem.getMold(stack);
        }

        if (moldInBasin == null)
            return 0;

        List<ItemStack> recipeOutputItems = new ArrayList<>();

        if (!simulate)
            recipeOutputItems.addAll(rollResults(basin.getLevel().random));

        if (!basin.acceptOutputs(recipeOutputItems, List.of(), false))
            return 0;

        return getFluidIngredients().get(0).amount();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    public static class Serializer implements RecipeSerializer<CastingRecipe> {
        private final MapCodec<CastingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> streamCodec;

        public Serializer() {
            this.codec = MoldRecipeParams.CODEC.xmap(CastingRecipe::new, CastingRecipe::getParams)
                    .validate(recipe -> {
                        var errors = recipe.validate();
                        if (errors.isEmpty())
                            return DataResult.success(recipe);
                        errors.add(recipe.getClass().getSimpleName() + " failed validation:");
                        return DataResult.error(() -> Joiner.on('\n').join(errors), recipe);
                    });
            this.streamCodec = MoldRecipeParams.STREAM_CODEC.map(CastingRecipe::new, CastingRecipe::getParams);
        }

        @Override
        public MapCodec<CastingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CastingRecipe> streamCodec() {
            return streamCodec;
        }

    }
}
