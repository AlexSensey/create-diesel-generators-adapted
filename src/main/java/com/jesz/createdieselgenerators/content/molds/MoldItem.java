package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class MoldItem extends Item {

    public MoldItem(Properties properties) {
        super(properties);
    }

    public static MoldType getMold(ItemStack stack) {
        if (!stack.has(CDGDataComponents.MOLD_TYPE))
            return null;
        return MoldType.findById(stack.get(CDGDataComponents.MOLD_TYPE));
    }

    @OnlyIn(Dist.CLIENT)
    public void registerExtension(RegisterClientExtensionsEvent event) {
        event.registerItem(SimpleCustomRenderer.create(this, new MoldItemRenderer()), this);
    }

    @Override
    public Component getName(ItemStack stack) {
        MoldType type = getMold(stack);
        if(type == null)
            return super.getName(stack);
        return Component.translatable("mold."+type.id.getNamespace()+"."+type.id.getPath());
    }
}
