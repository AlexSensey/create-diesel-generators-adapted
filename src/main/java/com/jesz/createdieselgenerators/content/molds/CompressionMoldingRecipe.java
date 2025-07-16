package com.jesz.createdieselgenerators.content.molds;

import com.google.common.base.Joiner;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CompressionMoldingRecipe extends CompactingRecipe {
    public MoldType moldType;
    MoldRecipeParams params;

    public CompressionMoldingRecipe(MoldRecipeParams params) {
        super(params);
        this.moldType = MoldType.findById(params.mold());
        this.params = params;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDGRecipes.COMPRESSION_MOLDING.getSerializer();
    }

    @Override
    public RecipeType<CompressionMoldingRecipe> getType() {
        return CDGRecipes.COMPRESSION_MOLDING.getType();
    }

    @Override
    public MoldRecipeParams getParams() {
        return params;
    }

    public static class Serializer implements RecipeSerializer<CompressionMoldingRecipe> {
        private final MapCodec<CompressionMoldingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, CompressionMoldingRecipe> streamCodec;

        public Serializer() {
            this.codec = MoldRecipeParams.CODEC.xmap(CompressionMoldingRecipe::new, CompressionMoldingRecipe::getParams)
                    .validate(recipe -> {
                        var errors = recipe.validate();
                        if (errors.isEmpty())
                            return DataResult.success(recipe);
                        errors.add(recipe.getClass().getSimpleName() + " failed validation:");
                        return DataResult.error(() -> Joiner.on('\n').join(errors), recipe);
                    });
            this.streamCodec = MoldRecipeParams.STREAM_CODEC.map(CompressionMoldingRecipe::new, CompressionMoldingRecipe::getParams);
        }

        @Override
        public MapCodec<CompressionMoldingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CompressionMoldingRecipe> streamCodec() {
            return streamCodec;
        }

    }
}
