package com.jesz.createdieselgenerators.content.tools.lighter;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.api.lang.Lang;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum LighterState implements StringRepresentable {
    CLOSED,
    OPEN,
    OPEN_IGNITED;


    public static final Codec<LighterState> CODEC = StringRepresentable.fromValues(LighterState::values);
    public static final StreamCodec<ByteBuf, LighterState> STREAM_CODEC = CatnipStreamCodecBuilders.ofEnum(LighterState.class);


    @Override
    public @NonNull String getSerializedName() {
        return Lang.asId(name());
    }
}
