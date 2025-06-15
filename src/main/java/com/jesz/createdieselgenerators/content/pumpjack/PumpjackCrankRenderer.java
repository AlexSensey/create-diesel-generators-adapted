package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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
    protected void renderSafe(PumpjackCrankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;
        BlockState blockState = be.getBlockState();
        BlockPos pos = be.getBlockPos();
        float angle = AngleHelper.angleLerp(partialTicks, be.prevAngle, be.angle);

        boolean isXAxis = blockState.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X;
        double v = ((isXAxis ? angle : -angle) + 90) / 180 * Math.PI;

        double sin = Math.sin(v) * (be.crankSize.getValue() == 0 ? 0.8125 : 1.125);
        double cos = Math.cos(v) * (be.crankSize.getValue() == 0 ? 0.8125 : 1.125);
        SuperByteBuffer crank = CachedBuffers.partial(be.crankSize.getValue() == 0 ? CDGPartialModels.PUMPJACK_CRANK_SMALL : CDGPartialModels.PUMPJACK_CRANK_LARGE, blockState);
        SuperByteBuffer rod = CachedBuffers.partial(be.crankSize.getValue() == 0 ? CDGPartialModels.PUMPJACK_CRANK_ROD_SMALL : CDGPartialModels.PUMPJACK_CRANK_ROD_LARGE, blockState);
        if(be.bearingPos == null) {
            if(isXAxis) {
                crank.translate(0.5, 1.25, 0).rotateZDegrees(angle);
            }else {
                crank.translate(0, 1.25, 0.5).rotateYDegrees(90).rotateZDegrees(angle);
            }

            double dstY = -1000-sin-1.25 - pos.getY();
            double dstX = pos.getX()-cos-0.5 - pos.getX();
            double dstZ = pos.getZ()-cos-0.5 - pos.getZ();

            if(isXAxis) {
                rod.translate(0.5, 1.25, 0).translate(cos, sin, 0).rotateZDegrees((float) (Math.atan2(dstY, dstX)*180/Math.PI-90));
            }else {
                rod.translate(0, 1.25, 0.5).translate(0, sin, cos).rotateYDegrees(90).rotateZDegrees((float) (Math.atan2(dstZ, dstY)*180/Math.PI));
            }
            rod.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
            crank.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        PumpjackBearingBlockEntity bearing = be.bearing.get();
        float interpolatedAngle = 0;
        if(bearing != null)
            interpolatedAngle = bearing.getInterpolatedAngle(partialTicks);
        if (be.inPonderAngle != Integer.MIN_VALUE){
            interpolatedAngle = be.inPonderAngle;
        }
        if (!isXAxis)
            interpolatedAngle *= -1;
        Vec2 crankBearingLocation = new Vec2(
                (float) ((be.crankBearingLocation.x) * Math.cos(interpolatedAngle/180 * Math.PI) - (be.crankBearingLocation.y) * Math.sin(interpolatedAngle/180*Math.PI))+0.5f,
                (float) ((be.crankBearingLocation.x) * Math.sin(interpolatedAngle/180 * Math.PI) + (be.crankBearingLocation.y) * Math.cos(interpolatedAngle/180*Math.PI))+0.5f);
        if(isXAxis)
            crankBearingLocation = crankBearingLocation.add(new Vec2((float) be.bearingPos.getX(), (float) be.bearingPos.getY()));
        else
            crankBearingLocation = crankBearingLocation.add(new Vec2((float) be.bearingPos.getZ(), (float) be.bearingPos.getY()));
        if(isXAxis) {
            crank.translate(0.5, 1.25, 0).rotateZDegrees(angle);
        }else {
            crank.translate(0, 1.25, 0.5).rotateYDegrees(90).rotateZDegrees(angle);
        }


        double dstY = crankBearingLocation.y-sin-1.25 - pos.getY();
        double dstX = crankBearingLocation.x-cos-0.5 - pos.getX();
        double dstZ = crankBearingLocation.x-cos-0.5 - pos.getZ();

        if(isXAxis) {
            rod.translate(0.5, 1.25, 0).translate(cos, sin, 0).rotateZDegrees((float) (Math.atan2(dstY, dstX)*180/Math.PI-90));
        }else {
            rod.translate(0, 1.25, 0.5).translate(0, sin, cos).rotateYDegrees(90).rotateZDegrees((float) (Math.atan2(dstZ, dstY)*180/Math.PI));
        }
        rod.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        crank.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }


}
