//package com.jesz.createdieselgenerators.mixins;
//
//import com.jesz.createdieselgenerators.content.entity_filter.EntityAttribute;
//import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
//import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraftforge.network.NetworkEvent;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(FilterScreenPacket.class)
//public class FilterScreenPacketMixin {
//    @Shadow @Final private FilterScreenPacket.Option option;
//
//    @Shadow @Final private CompoundTag data;
//
//    @Inject(method = "lambda$handle$0", at = @At("TAIL"), remap = false)
//    public void handle(NetworkEvent.Context context, CallbackInfo ci){
//        ServerPlayer player = context.getSender();
//        if(player.containerMenu instanceof EntityFilterMenu menu){
//            if (option == FilterScreenPacket.Option.WHITELIST)
//                menu.whitelist = AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ;
//            if (option == FilterScreenPacket.Option.WHITELIST2)
//                menu.whitelist = AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ;
//            if (option == FilterScreenPacket.Option.BLACKLIST)
//                menu.whitelist = AttributeFilterMenu.WhitelistMode.BLACKLIST;
//            if (option == FilterScreenPacket.Option.ADD_TAG)
//                menu.appendSelectedAttribute(EntityAttribute.fromNBT(data), false);
//            if (option == FilterScreenPacket.Option.ADD_INVERTED_TAG)
//                menu.appendSelectedAttribute(EntityAttribute.fromNBT(data), true);
//        }
//    }
//}
