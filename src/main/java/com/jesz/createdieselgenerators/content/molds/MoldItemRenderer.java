package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class MoldItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        MoldType type = MoldItem.getMold(stack);
        if (type == null || type.model == null) {
            renderer.render(model.getOriginalModel(), light);
            return;
        }
        renderer.render(type.model, light);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderInBasin(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack) {
        MoldType type = MoldItem.getMold(stack);
        if (type == null)
            return;
        ms.pushPose();
        TransformStack<PoseTransformStack> msr = TransformStack.of(ms);
        msr.translate(0.5, 0.7, 0.5)
                .rotateXDegrees(90)
                .scale(1.75f)
                .translate(0, -0.125,0);

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, Minecraft.getInstance().level, 0);
        ms.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderItemsOnMold(BasinBlockEntity basin, PoseStack ms, MultiBufferSource buffer, int light, int overlay, List<ItemStack> items, float partialTicks) {

        FilteringRenderer.renderOnBlockEntity(basin, partialTicks, ms, buffer, light, overlay);
        RandomSource r = RandomSource.create(basin.getBlockPos().hashCode());

        for(ItemStack stack : items){
            if (CDGItems.MOLD.isIn(stack)) {
                renderInBasin(ms, buffer, light, overlay, stack);
                continue;
            }
            ms.pushPose();
            TransformStack<PoseTransformStack> msr = TransformStack.of(ms);

            msr.translate(0.5, 0.74, 0.5)
                    .rotateXDegrees(90).scale(0.5f);
            msr.translate(VecHelper.offsetRandomly(Vec3.ZERO, r, (float) 1 /16));

            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, ms, buffer, Minecraft.getInstance().level, 0);
            ms.popPose();
        }
    }
}
