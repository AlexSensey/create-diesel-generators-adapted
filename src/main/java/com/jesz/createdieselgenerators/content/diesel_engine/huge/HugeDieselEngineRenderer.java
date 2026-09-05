package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.foundation.ScrollOptionOverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;

public class HugeDieselEngineRenderer extends SafeBlockEntityRenderer<HugeDieselEngineBlockEntity> {
    public HugeDieselEngineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(HugeDieselEngineBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new HugeEngineRenderState();
    }

    @Override
    public void extractRenderState(HugeDieselEngineBlockEntity be, BlockEntityRenderState state,
                                   float partialTicks, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (state instanceof HugeEngineRenderState engineState) {
            engineState.blockEntity = be;
            engineState.partialTicks = partialTicks;
        }
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof HugeEngineRenderState engineState) || engineState.blockEntity == null)
            return;
        HugeDieselEngineBlockEntity be = engineState.blockEntity;
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(FACING);

        ScrollOptionOverlayRenderer.render(be, be.movementDirection, ms, collector);
        // Keep upgrades in the same rendering path as the engine assembly. Flywheel's
        // visual owns the silencer while visualization is active; this renderer is the
        // compatibility fallback only.
        if (be.upgrade == EngineUpgrades.SILENCER
                && !VisualizationManager.supportsVisualization(be.getLevel()))
            submitUpgrade(be, facing, state, ms, collector);

        // Flywheel owns the moving assembly whenever visualization is active.
        if (VisualizationManager.supportsVisualization(be.getLevel()))
            return;

        Float angle = HugeDieselEngineClient.getTargetAngle(be);
        PoweredEngineShaftBlockEntity shaft = be.getShaft();
        if (angle == null || shaft == null) {
            ms.pushPose();
            transform(ms, facing, false);
            ms.translate(0, .53475, 0);
            submitPart(CDGPartialModels.ENGINE_PISTON, ms, collector, state.lightCoords);
            ms.popPose();
            return;
        }

        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        Direction.Axis facingAxis = facing.getAxis();
        boolean roll90 = facingAxis.isHorizontal() && axis == Direction.Axis.Y
                || facingAxis.isVertical() && axis == Direction.Axis.Z;
        float shaftRotation = facing == Direction.DOWN ? -90 : facing == Direction.UP ? 90
                : facing == Direction.WEST ? -90 : facing == Direction.EAST ? 90 : 0;
        if (roll90)
            shaftRotation = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0
                    : facing == Direction.EAST ? -90 : facing == Direction.WEST ? 90 : 0;
        angle += shaftRotation * Mth.DEG_TO_RAD;

        float directionSign = facingAxis == Direction.Axis.Y ? -1 : 1;
        float sine = Mth.sin(angle) * directionSign;
        float sine2 = Mth.sin(angle - Mth.HALF_PI) * directionSign;
        float piston = ((1 - sine) / 4) + .4375f;

        ms.pushPose();
        transform(ms, facing, roll90);
        ms.translate(0, piston, 0);
        submitPart(CDGPartialModels.ENGINE_PISTON, ms, collector, state.lightCoords);
        ms.popPose();

        ms.pushPose();
        transform(ms, facing, roll90);
        ms.translate(.5, .5, .5);
        ms.translate(0, 1, 0);
        ms.translate(-.5, -.5, -.5);
        ms.translate(0, piston, 0);
        ms.translate(0, 4 / 16f, 8 / 16f);
        ms.mulPose(Axis.XP.rotationDegrees(sine2 * 23f));
        ms.translate(0, -4 / 16f, -8 / 16f);
        submitPart(CDGPartialModels.ENGINE_PISTON_LINKAGE, ms, collector, state.lightCoords);
        ms.popPose();

        if (shaft.isEngineForConnectorDisplay(be.getBlockPos())) {
            ms.pushPose();
            transform(ms, facing, roll90);
            ms.translate(0, 2, 0);
            ms.translate(.5, .5, .5);
            float connectorAngle = -angle + Mth.HALF_PI
                    - (facingAxis.isVertical() ? Mth.PI : 0);
            ms.mulPose(Axis.XP.rotation(connectorAngle));
            ms.translate(-.5, -.5, -.5);
            submitPart(CDGPartialModels.ENGINE_PISTON_CONNECTOR, ms, collector, state.lightCoords);
            ms.popPose();
        }
    }

    private static void transform(PoseStack ms, Direction facing, boolean roll90) {
        ms.translate(.5, .5, .5);
        ms.mulPose(Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
        ms.mulPose(Axis.XP.rotationDegrees(AngleHelper.verticalAngle(facing) + 90));
        if (roll90)
            ms.mulPose(Axis.YP.rotationDegrees(-90));
        ms.translate(-.5, -.5, -.5);
    }

    private static void submitUpgrade(HugeDieselEngineBlockEntity be, Direction facing,
                                      BlockEntityRenderState state, PoseStack ms,
                                      SubmitNodeCollector collector) {
        ms.pushPose();
        ms.translate(.5, .5, .5);
        if (facing.getAxis().isVertical()) {
            ms.mulPose(Axis.ZP.rotationDegrees(90));
            ms.mulPose(Axis.YP.rotationDegrees(
                    facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 90));
        } else {
            ms.mulPose(Axis.YP.rotationDegrees(
                    facing.getAxis() == Direction.Axis.X ? facing.toYRot() : facing.toYRot() + 180));
        }
        ms.translate(-.5, -.5, -.5);
        submitPart(CDGPartialModels.HUGE_ENGINE_SILENCER, ms, collector, state.lightCoords);
        ms.popPose();
    }

    private static void submitPart(PartialModel model, PoseStack ms, SubmitNodeCollector collector, int light) {
        BlockStateModelPart part = model.get();
        if (part == null)
            return;
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(part),
                BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    private static class HugeEngineRenderState extends BlockEntityRenderState {
        private HugeDieselEngineBlockEntity blockEntity;
        @SuppressWarnings("unused")
        private float partialTicks;
    }
}
