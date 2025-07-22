package com.jesz.createdieselgenerators.content.track_layers_bag;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record TrackLayersBagItemDataComponent(ItemStack stack, int count) {
    public static final Codec<TrackLayersBagItemDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(TrackLayersBagItemDataComponent::stack),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").forGetter(TrackLayersBagItemDataComponent::count)
            )
            .apply(instance, TrackLayersBagItemDataComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrackLayersBagItemDataComponent> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC, TrackLayersBagItemDataComponent::stack,
                    ByteBufCodecs.VAR_INT, TrackLayersBagItemDataComponent::count,
                    TrackLayersBagItemDataComponent::new);

    public TrackLayersBagItemDataComponent(ItemStack stack) {
        this(stack.copyWithCount(1), stack.getCount());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrackLayersBagItemDataComponent t))
            return false;
        return t.count() == count() && ItemStack.isSameItemSameComponents(stack, t.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count(), stack().getComponents(), stack().getItem());
    }
}
