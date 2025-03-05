package com.jesz.createdieselgenerators.content.molds;

import com.google.gson.JsonObject;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CompressionMoldingRecipe extends CompactingRecipe {
    public MoldType moldType;
    public CompressionMoldingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(params);
    }

    @Override
    public void readAdditional(JsonObject json) {
        super.readAdditional(json);
        moldType = MoldType.findById(new ResourceLocation(json.get("mold").getAsString()));
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        super.readAdditional(buffer);
        moldType = MoldType.findById(new ResourceLocation(buffer.readUtf()));
    }

    @Override
    public void writeAdditional(JsonObject json) {
        super.writeAdditional(json);
        json.addProperty("mold", moldType.getId().toString());
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        super.writeAdditional(buffer);
        buffer.writeUtf(moldType.getId().toString());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CDGRecipes.COMPRESSION_MOLDING.getSerializer();
    }

    @Override
    public RecipeType<CompressionMoldingRecipe> getType() {
        return CDGRecipes.COMPRESSION_MOLDING.getType();
    }
}
