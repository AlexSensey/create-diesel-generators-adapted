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
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MoldItemRenderer extends CustomRenderedItemModelRenderer {
    private static final Map<net.minecraft.resources.Identifier, PartialModel> MODELS = new HashMap<>();

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        MoldType type = MoldItem.getMold(stack);
        if (type == null) {
            renderer.render(model.getOriginalModel(), light);
            return;
        }
        com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility
                .partial(MODELS.computeIfAbsent(type.getModelId(), PartialModel::of), Blocks.AIR.defaultBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

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

        // Nested item submission is handled by the 26.2 basin render-state path.
        ms.popPose();
    }

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

            // Nested item submission is handled by the 26.2 basin render-state path.
            ms.popPose();
        }
    }
}
