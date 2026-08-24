package com.jesz.createdieselgenerators.content.pumpjack;

import java.util.List;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.foundation.ScrollOptionOverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class PumpjackCrankRenderer extends ShaftRenderer<PumpjackCrankBlockEntity> {
    public PumpjackCrankRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof KineticRenderState kineticState)
                || !(kineticState.blockEntity instanceof PumpjackCrankBlockEntity be)
                || isInvalid(be))
            return;

        ScrollOptionOverlayRenderer.render(be, be.crankSize, ms, collector);
        submitShaft(be, kineticState.partialTicks, state, ms, collector);

        BlockState blockState = be.getBlockState();
        BlockPos pos = be.getBlockPos();
        float angle = AngleHelper.angleLerp(kineticState.partialTicks, be.prevAngle, be.angle);
        boolean isXAxis = blockState.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
        double v = ((isXAxis ? angle : -angle) + 90) / 180 * Math.PI;
        double radius = be.crankSize.getValue() == 0 ? .8125 : 1.125;
        double sin = Math.sin(v) * radius;
        double cos = Math.cos(v) * radius;

        double dstY = -1000 - sin - 1.25 - pos.getY();
        double dstX = -.5 - cos;
        double dstZ = -.5 - cos;
        if (be.bearingPos != null) {
            PumpjackBearingBlockEntity bearing = be.bearing.get();
            float bearingAngle = bearing == null ? 0 : bearing.getInterpolatedAngle(kineticState.partialTicks);
            if (be.inPonderAngle != Integer.MIN_VALUE)
                bearingAngle = be.inPonderAngle;
            if (!isXAxis)
                bearingAngle *= -1;
            Vec2 location = new Vec2(
                    (float) (be.crankBearingLocation.x * Math.cos(Math.toRadians(bearingAngle))
                            - be.crankBearingLocation.y * Math.sin(Math.toRadians(bearingAngle))) + .5f,
                    (float) (be.crankBearingLocation.x * Math.sin(Math.toRadians(bearingAngle))
                            + be.crankBearingLocation.y * Math.cos(Math.toRadians(bearingAngle))) + .5f);
            location = location.add(new Vec2(isXAxis ? be.bearingPos.getX() : be.bearingPos.getZ(), be.bearingPos.getY()));
            dstY = location.y - sin - 1.25 - pos.getY();
            dstX = location.x - cos - .5 - pos.getX();
            dstZ = location.x - cos - .5 - pos.getZ();
        }

        PartialModel crankModel = be.crankSize.getValue() == 0
                ? CDGPartialModels.PUMPJACK_CRANK_SMALL : CDGPartialModels.PUMPJACK_CRANK_LARGE;
        PartialModel rodModel = be.crankSize.getValue() == 0
                ? CDGPartialModels.PUMPJACK_CRANK_ROD_SMALL : CDGPartialModels.PUMPJACK_CRANK_ROD_LARGE;

        ms.pushPose();
        if (isXAxis)
            ms.translate(.5, 1.25, 0);
        else {
            ms.translate(0, 1.25, .5);
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        }
        ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
        submitPart(crankModel, state, ms, collector);
        ms.popPose();

        ms.pushPose();
        if (isXAxis) {
            ms.translate(.5 + cos, 1.25 + sin, 0);
            ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) Math.toDegrees(Math.atan2(dstY, dstX)) - 90));
        } else {
            ms.translate(0, 1.25 + sin, .5 + cos);
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) Math.toDegrees(Math.atan2(dstZ, dstY))));
        }
        submitPart(rodModel, state, ms, collector);
        ms.popPose();
    }

    private void submitShaft(PumpjackCrankBlockEntity be, float partialTicks, BlockEntityRenderState state,
                             PoseStack ms, SubmitNodeCollector collector) {
        BlockState renderedState = getRenderedBlockState(be);
        List<BlockStateModelPart> parts = getRotatingModelParts(be, renderedState);
        if (parts.isEmpty())
            return;
        ms.pushPose();
        transformRotatingModel(be, ms, partialTicks);
        collector.submitBlockModel(ms, getRotatingRenderType(parts), parts, BlockModelRenderState.EMPTY_TINTS,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    private static void submitPart(PartialModel model, BlockEntityRenderState state, PoseStack ms,
                                   SubmitNodeCollector collector) {
        BlockStateModelPart part = model.get();
        if (part != null)
            collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(part),
                    BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    protected void renderSafe(PumpjackCrankBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }
}
