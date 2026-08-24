package com.jesz.createdieselgenerators.content.tools.wire_cutters;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class WireCuttersItemRenderer extends CustomRenderedItemModelRenderer {
    static final PartialModel OPEN_MODEL = PartialModel.of(CreateDieselGenerators.id("item/wire_cutters_cut"));
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Player player = Minecraft.getInstance().player;
        if (!stack.has(CDGDataComponents.PROCESSING_ITEM) || player == null)
            renderer.render(model.getOriginalModel(), light);
        else {
            float time = ((AnimationTickHolder.getTicks() + AnimationTickHolder.getPartialTicks()) % 10) / 10;
            ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();

            ms.pushPose();
            TransformStack.of(ms)
                    .translate(0.1, 0.2, 0)
                    .rotateZDegrees((float) ((AnimationTickHolder.getTicks() + 5) / 10) * -30);
            ms.popPose();
            ms.pushPose();

            TransformStack.of(ms)
                    .translate(0, 0, 0.1)
                    .rotateYDegrees(32);
            if (time > 0.5)
                renderer.render(model.getOriginalModel(), light);
            else
                com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility
                        .partial(OPEN_MODEL, Blocks.AIR.defaultBlockState())
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            ms.popPose();
        }
    }
}
