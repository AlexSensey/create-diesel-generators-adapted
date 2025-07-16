package com.jesz.createdieselgenerators.content.tools.wire_cutters;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WireCuttersItemRenderer extends CustomRenderedItemModelRenderer {
    static final PartialModel OPEN_MODEL = PartialModel.of(CreateDieselGenerators.rl("item/wire_cutters_cut"));
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Player player = Minecraft.getInstance().player;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();


        if (!stack.has(CDGDataComponents.PROCESSING_ITEM) || player == null)
            renderer.render(model.getOriginalModel(), light);
        else {
            float time = ((AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks()) % 10) / 10;
            ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();

            ms.pushPose();
            TransformStack.of(ms)
                    .translate(0.1, 0.2, 0)
                    .rotateZDegrees((float) ((AnimationTickHolder.getTicks() + 5) / 10) * -30);
            itemRenderer.renderStatic(processingItem, ItemDisplayContext.GUI, light, overlay, ms, buffer, Minecraft.getInstance().level, 0);

            ms.popPose();
            ms.pushPose();

            TransformStack.of(ms)
                    .translate(0, 0, 0.1)
                    .rotateYDegrees(32);
            if (time > 0.5)
                renderer.render(model.getOriginalModel(), light);
            else
                renderer.render(OPEN_MODEL.get(), light);
            ms.popPose();
        }
    }
}
