package com.jesz.createdieselgenerators.content.diesel_engine.modular;

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

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;

public class ModularDieselEngineRenderer extends ShaftRenderer<ModularDieselEngineBlockEntity> {

    public ModularDieselEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        super.submit(state, ms, collector, cameraRenderState);
        if (!(state instanceof KineticRenderState kineticState)
                || !(kineticState.blockEntity instanceof ModularDieselEngineBlockEntity be)
                || isInvalid(be))
            return;

        ScrollOptionOverlayRenderer.render(be, be.movementDirection, ms, collector);
        float radians = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(),
                KineticBlockEntityRenderer.getRotationAxisOf(be), kineticState.partialTicks);
        int frame = (int) (Math.abs(Math.toDegrees(radians)) * 3 % 360) / 36;
        submitPart(selectPiston(frame), be, state, ms, collector, true);

        ModularDieselEngineBlockEntity controller = be.getControllerBE();
        EngineUpgrades upgrade = controller == null ? be.upgrade : controller.upgrade;
        PartialModel upgradeModel = selectUpgrade(upgrade);
        if (upgradeModel != null)
            submitPart(upgradeModel, be, state, ms, collector, false);
    }

    private static PartialModel selectPiston(int frame) {
        return switch (frame) {
            case 9, 2 -> CDGPartialModels.MODULAR_ENGINE_PISTONS_1;
            case 8, 3 -> CDGPartialModels.MODULAR_ENGINE_PISTONS_2;
            case 7, 4 -> CDGPartialModels.MODULAR_ENGINE_PISTONS_3;
            case 6, 5 -> CDGPartialModels.MODULAR_ENGINE_PISTONS_4;
            default -> CDGPartialModels.MODULAR_ENGINE_PISTONS_0;
        };
    }

    private static PartialModel selectUpgrade(EngineUpgrades upgrade) {
        if (upgrade == EngineUpgrades.SILENCER)
            return CDGPartialModels.MODULAR_ENGINE_SILENCER;
        if (upgrade == EngineUpgrades.TURBOCHARGER)
            return CDGPartialModels.MODULAR_TURBOCHARGER;
        return null;
    }

    private static void submitPart(PartialModel model, ModularDieselEngineBlockEntity be,
                                   BlockEntityRenderState state, PoseStack ms,
                                   SubmitNodeCollector collector, boolean piston) {
        BlockStateModelPart part = model.get();
        if (part == null)
            return;
        Direction facing = be.getBlockState().getValue(FACING);
        ms.pushPose();
        ms.translate(.5, .5, .5);
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(facing.toYRot()));
        ms.translate(-.5, -.5, -.5);
        collector.submitBlockModel(ms, piston ? RenderTypes.solidMovingBlock() : RenderTypes.cutoutMovingBlock(), List.of(part),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(ModularDieselEngineBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }
}
