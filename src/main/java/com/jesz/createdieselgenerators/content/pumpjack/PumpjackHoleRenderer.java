package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class PumpjackHoleRenderer extends SafeBlockEntityRenderer<PumpjackHoleBlockEntity> {
    public PumpjackHoleRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(PumpjackHoleBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        CachedBuffers.partial(CDGPartialModels.PUMPJACK_ROPE, be.getBlockState())
                .translate(0.5, 0, 0.5)
                .scale(1, be.pipeLength, 1)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
