package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGCreativeTab;
import com.jesz.createdieselgenerators.CDGItems;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class MoldItem extends Item {

    public MoldItem(Properties properties) {
        super(properties);
    }

    public static MoldType getMold(ItemStack stack){
        CompoundTag tag = stack.getTag();
        if(tag == null)
            return null;
        return MoldType.findById(new ResourceLocation(tag.getString("Mold")));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new MoldItemRenderer()));
    }

    @Override
    public Component getName(ItemStack stack) {
        MoldType type = getMold(stack);
        if(type == null)
            return super.getName(stack);
        return Component.translatable("mold."+type.id.getNamespace()+"."+type.id.getPath());
    }
}
