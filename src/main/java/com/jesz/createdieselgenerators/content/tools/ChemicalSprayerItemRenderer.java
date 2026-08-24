package com.jesz.createdieselgenerators.content.tools;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class ChemicalSprayerItemRenderer extends CustomRenderedItemModelRenderer {
    protected static final PartialModel COG = PartialModel.of(CreateDieselGenerators.id("item/chemical_sprayer/cog"));

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);
        LocalPlayer player = Minecraft.getInstance().player;

        float worldTime = AnimationTickHolder.getRenderTime() / 10;
        float angle = worldTime * ((player != null && player.isUsingItem()
                && player.getItemInHand(player.getUsedItemHand()) == stack) ? -200 : -25);
        angle %= 360;

        ms.pushPose();
        ms.mulPose(Axis.ZP.rotationDegrees(angle));
        ms.translate(0.5, 0.5, 0.53125);
        com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility
                .partial(COG, Blocks.AIR.defaultBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        ms.popPose();
    }
}
