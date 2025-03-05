package com.jesz.createdieselgenerators.packets;

import com.jesz.createdieselgenerators.content.entity_filter.EntityAttribute;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class EntityFilterScreenPacket extends SimplePacketBase {
    private final FilterScreenPacket.Option option;
    private final CompoundTag data;
    public EntityFilterScreenPacket(FilterScreenPacket.Option option) {
        this(option, new CompoundTag());
    }

    public EntityFilterScreenPacket(FilterScreenPacket.Option option, CompoundTag data) {
        this.option = option;
        this.data = data;
    }

    public EntityFilterScreenPacket(FriendlyByteBuf buffer) {
        option = FilterScreenPacket.Option.values()[buffer.readInt()];
        data = buffer.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(option.ordinal());
        buffer.writeNbt(data);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null)
                return;

            if (player.containerMenu instanceof EntityFilterMenu c) {
                if (option == FilterScreenPacket.Option.WHITELIST)
                    c.whitelist = AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ;
                if (option == FilterScreenPacket.Option.WHITELIST2)
                    c.whitelist = AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ;
                if (option == FilterScreenPacket.Option.BLACKLIST)
                    c.whitelist = AttributeFilterMenu.WhitelistMode.BLACKLIST;
                if (option == FilterScreenPacket.Option.ADD_TAG)
                    c.appendSelectedAttribute(EntityAttribute.fromNBT(data), false);
                if (option == FilterScreenPacket.Option.ADD_INVERTED_TAG)
                    c.appendSelectedAttribute(EntityAttribute.fromNBT(data), true);
            }
        });
        return true;
    }
}
