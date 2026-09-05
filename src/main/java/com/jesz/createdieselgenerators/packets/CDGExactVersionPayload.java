package com.jesz.createdieselgenerators.packets;

import com.jesz.createdieselgenerators.CreateDieselGenerators;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Required handshake marker that rejects mismatched Diesel Generators builds. */
public enum CDGExactVersionPayload implements CustomPacketPayload {
    INSTANCE;

    public static final Type<CDGExactVersionPayload> TYPE =
            new Type<>(CreateDieselGenerators.id("exact_version"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CDGExactVersionPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public static void register(IEventBus modEventBus, String version) {
        modEventBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar(version)
                .playBidirectional(TYPE, STREAM_CODEC, CDGExactVersionPayload::handle,
                        CDGExactVersionPayload::handle));
    }

    private static void handle(CDGExactVersionPayload payload, IPayloadContext context) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
