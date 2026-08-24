package com.jesz.createdieselgenerators.content.pumpjack;

import java.util.List;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

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
import net.minecraft.world.phys.Vec3;

public class PumpjackHoleRenderer extends SafeBlockEntityRenderer<PumpjackHoleBlockEntity> {
    public PumpjackHoleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new HoleRenderState();
    }

    @Override
    public void extractRenderState(PumpjackHoleBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (state instanceof HoleRenderState holeState)
            holeState.pipeLength = be.pipeLength;
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof HoleRenderState holeState) || holeState.pipeLength <= 0)
            return;
        BlockStateModelPart rope = CDGPartialModels.PUMPJACK_ROPE.get();
        if (rope == null)
            return;
        ms.pushPose();
        ms.translate(.5, 0, .5);
        ms.scale(1, holeState.pipeLength, 1);
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(rope),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(PumpjackHoleBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }

    private static class HoleRenderState extends BlockEntityRenderState {
        private int pipeLength;
    }
}
