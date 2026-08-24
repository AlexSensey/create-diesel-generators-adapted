package com.jesz.createdieselgenerators.content.molds;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public class MoldRecipeParams extends ProcessingRecipeParams {
    public static MapCodec<MoldRecipeParams> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            codec(MoldRecipeParams::new).forGetter(Function.identity()),
            Identifier.CODEC.fieldOf("mold").forGetter(MoldRecipeParams::mold)
    ).apply(i, (params, mold) -> {
        params.mold = mold;
        return params;
    }));

    public static StreamCodec<RegistryFriendlyByteBuf, MoldRecipeParams> STREAM_CODEC = streamCodec(MoldRecipeParams::new);

    protected Identifier mold;

    protected final Identifier mold() {
        return mold;
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        Identifier.STREAM_CODEC.encode(buffer, mold);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        mold = Identifier.STREAM_CODEC.decode(buffer);
    }
}
