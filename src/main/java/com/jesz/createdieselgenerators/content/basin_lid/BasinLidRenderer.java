package com.jesz.createdieselgenerators.content.basin_lid;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import static com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock.ON_A_BASIN;
import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class BasinLidRenderer extends SafeBlockEntityRenderer<BasinLidBlockEntity> {

    public BasinLidRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(BasinLidBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        if(!be.getBlockState().getValue(ON_A_BASIN))
            return;

        Direction facing = be.getBlockState().getValue(HORIZONTAL_FACING);

        CachedBuffers.partial(CDGPartialModels.SMALL_GAUGE_DIAL, be.getBlockState())
                .center()
                .rotateYDegrees(-facing.toYRot()+180)
                .translate(0.5625f, -0.375, 1.0625)
                .uncenter()
                .rotateZDegrees(be.progress*-90+90).renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
    }
}
