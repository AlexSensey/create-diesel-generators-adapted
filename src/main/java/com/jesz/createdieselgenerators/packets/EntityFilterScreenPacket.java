package com.jesz.createdieselgenerators.packets;

import com.jesz.createdieselgenerators.content.entity_filter.EntityAttribute;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record EntityFilterScreenPacket(FilterScreenPacket.Option option, EntityAttribute attribute) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, EntityFilterScreenPacket> STREAM_CODEC = StreamCodec.composite(
            FilterScreenPacket.Option.STREAM_CODEC, EntityFilterScreenPacket::option,
            EntityAttribute.STREAM_CODEC, EntityFilterScreenPacket::attribute,
            EntityFilterScreenPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        if (player == null)
            return;

        if (player.containerMenu instanceof EntityFilterMenu c) {
            if (option == FilterScreenPacket.Option.WHITELIST)
                c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
            if (option == FilterScreenPacket.Option.WHITELIST2)
                c.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
            if (option == FilterScreenPacket.Option.BLACKLIST)
                c.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;

            if (option == FilterScreenPacket.Option.ADD_TAG || option == FilterScreenPacket.Option.ADD_INVERTED_TAG)
                c.appendSelectedAttribute(attribute, option == FilterScreenPacket.Option.ADD_INVERTED_TAG);

        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return CDGPackets.ENTITY_FILTER_SCREEN;
    }
}
