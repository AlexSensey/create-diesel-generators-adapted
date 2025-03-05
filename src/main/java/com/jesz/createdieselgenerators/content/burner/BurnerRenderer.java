package com.jesz.createdieselgenerators.content.burner;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class BurnerRenderer extends ShaftRenderer<BurnerBlockEntity> {
    public BurnerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BurnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        BlockState state = be.getBlockState();
        float rotation = Mth.lerp(Mth.lerp(partialTicks, be.prevValveState, be.valveState), -45, 45);
        CachedBuffers.partial(CDGPartialModels.SMALL_GAUGE_DIAL, state)
                .center().rotateYDegrees(state.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0).uncenter()
                .translate(0.25, 0.25, 0.5)
                .rotateXDegrees(rotation)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        CachedBuffers.partial(CDGPartialModels.SMALL_GAUGE_DIAL, state)
                .center().rotateYDegrees(state.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0).uncenter()
                .translate(0.75, 0.25, 0.5)
                .rotateXDegrees(-rotation)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }
}
