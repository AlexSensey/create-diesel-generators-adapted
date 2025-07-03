package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.molds.MoldItemRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;

@Mixin(BasinRenderer.class)
public abstract class BasinRendererMixin {
    @Shadow protected abstract void renderItem(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack);

    @Inject(method = "renderItem", at=@At("HEAD"), remap = false, cancellable = true)
    public void renderItem(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack, CallbackInfo ci){
        if(!CDGItems.MOLD.isIn(stack))
            return;
        MoldItemRenderer.renderInBasin(ms, buffer, light, overlay, stack);
        ci.cancel();
    }

    @Inject(method = "renderSafe(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at=@At("HEAD"), remap = false, cancellable = true)
    public void renderSafe(BasinBlockEntity basin, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci){
//        items.addAll(((BasinBlockEntityAccessor)basin).getVisualizedInputItems())
        IItemHandlerModifiable inv = ((BasinBlockEntityAccessor) basin).getItemCapability().orElse(new ItemStackHandler());
        List<ItemStack> items = new LinkedList<>();
        boolean hasMold = false;
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            items.add(stack);
            if(CDGItems.MOLD.isIn(stack))
                hasMold = true;
        }
        if(!hasMold)
            return;
        MoldItemRenderer.renderItemsOnMold(basin, ms, buffer, light, overlay, items, partialTicks);

        ci.cancel();
    }

}
