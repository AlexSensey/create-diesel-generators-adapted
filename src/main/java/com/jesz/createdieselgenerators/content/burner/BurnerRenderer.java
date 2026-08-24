package com.jesz.createdieselgenerators.content.burner;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BurnerRenderer extends ShaftRenderer<BurnerBlockEntity> {
    public BurnerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        super.submit(state, ms, collector, cameraRenderState);
        if (!(state instanceof KineticRenderState kineticState)
                || !(kineticState.blockEntity instanceof BurnerBlockEntity be)
                || isInvalid(be))
            return;

        BlockStateModelPart dial = CDGPartialModels.SMALL_GAUGE_DIAL.get();
        if (dial == null)
            return;

        BlockState blockState = be.getBlockState();
        float rotation = Mth.lerp(Mth.lerp(kineticState.partialTicks, be.prevValveState, be.valveState), -45, 45);
        float yRotation = blockState.getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0;
        submitDial(dial, .25f, rotation, yRotation, state, ms, collector);
        submitDial(dial, .75f, -rotation, yRotation, state, ms, collector);
    }

    private static void submitDial(BlockStateModelPart dial, float x, float rotation, float yRotation,
                                   BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector) {
        ms.pushPose();
        ms.translate(.5, .5, .5);
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRotation));
        ms.translate(-.5, -.5, -.5);
        ms.translate(x, .25, .5);
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotation));
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(dial),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(BurnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }
}
