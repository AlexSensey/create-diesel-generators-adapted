package com.jesz.createdieselgenerators.content.diesel_engine.normal;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.foundation.ScrollOptionOverlayRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public class DieselEngineRenderer extends ShaftRenderer<DieselEngineBlockEntity> {

    public DieselEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        super.submit(state, ms, collector, cameraRenderState);
        if (!(state instanceof KineticRenderState kineticState)
                || !(kineticState.blockEntity instanceof DieselEngineBlockEntity be)
                || isInvalid(be))
            return;

        ScrollOptionOverlayRenderer.render(be, be.movementDirection, ms, collector);
        float radians = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(),
                KineticBlockEntityRenderer.getRotationAxisOf(be), kineticState.partialTicks);
        int frame = (int) (Math.abs(Math.toDegrees(radians)) * 3 % 360) / 36;
        submitPart(selectPiston(frame, be.getBlockState().getValue(FACING)), be, state, ms, collector, true);

        PartialModel upgradeModel = selectUpgrade(be.upgrade, be.getBlockState().getValue(FACING));
        if (upgradeModel != null)
            submitPart(upgradeModel, be, state, ms, collector, false);
    }

    private static PartialModel selectPiston(int frame, Direction facing) {
        int model = switch (frame) {
            case 2, 9 -> 1;
            case 3, 8 -> 2;
            case 4, 7 -> 3;
            case 5, 6 -> 4;
            default -> 0;
        };
        if (facing.getAxis().isVertical()) {
            return switch (model) {
                case 1 -> CDGPartialModels.ENGINE_PISTONS_VERTICAL_1;
                case 2 -> CDGPartialModels.ENGINE_PISTONS_VERTICAL_2;
                case 3 -> CDGPartialModels.ENGINE_PISTONS_VERTICAL_3;
                case 4 -> CDGPartialModels.ENGINE_PISTONS_VERTICAL_4;
                default -> CDGPartialModels.ENGINE_PISTONS_VERTICAL_0;
            };
        }
        return switch (model) {
            case 1 -> CDGPartialModels.ENGINE_PISTONS_1;
            case 2 -> CDGPartialModels.ENGINE_PISTONS_2;
            case 3 -> CDGPartialModels.ENGINE_PISTONS_3;
            case 4 -> CDGPartialModels.ENGINE_PISTONS_4;
            default -> CDGPartialModels.ENGINE_PISTONS_0;
        };
    }

    private static PartialModel selectUpgrade(EngineUpgrades upgrade, Direction facing) {
        if (upgrade == EngineUpgrades.SILENCER)
            return facing.getAxis().isVertical()
                    ? CDGPartialModels.ENGINE_SILENCER_VERTICAL : CDGPartialModels.ENGINE_SILENCER;
        if (upgrade == EngineUpgrades.TURBOCHARGER)
            return facing.getAxis().isVertical()
                    ? CDGPartialModels.ENGINE_TURBOCHARGER_VERTICAL : CDGPartialModels.ENGINE_TURBOCHARGER;
        return null;
    }

    private static void submitPart(PartialModel model, DieselEngineBlockEntity be, BlockEntityRenderState state,
                                   PoseStack ms, SubmitNodeCollector collector, boolean piston) {
        BlockStateModelPart part = model.get();
        if (part == null)
            return;
        Direction facing = be.getBlockState().getValue(FACING);

        ms.pushPose();
        ms.translate(.5, .5, .5);
        if (facing.getAxis().isHorizontal()) {
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(facing.toYRot()));
        } else if (piston) {
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(facing == Direction.DOWN ? 180 : 270));
            if (facing == Direction.DOWN)
                ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
        } else {
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(
                    facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 180));
        }
        ms.translate(-.5, -.5, -.5);
        collector.submitBlockModel(ms, piston ? RenderTypes.solidMovingBlock() : RenderTypes.cutoutMovingBlock(), List.of(part),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(DieselEngineBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }
}
