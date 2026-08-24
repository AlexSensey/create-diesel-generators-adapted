package com.jesz.createdieselgenerators.content.basin_lid;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock.ON_A_BASIN;
import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class BasinLidRenderer extends SafeBlockEntityRenderer<BasinLidBlockEntity> {

    public BasinLidRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new LidRenderState();
    }

    @Override
    public void extractRenderState(BasinLidBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (state instanceof LidRenderState lidState) {
            lidState.onBasin = be.getBlockState().getValue(ON_A_BASIN);
            lidState.facing = be.getBlockState().getValue(HORIZONTAL_FACING);
            lidState.progress = be.progress;
        }
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof LidRenderState lidState) || !lidState.onBasin)
            return;
        BlockStateModelPart dial = CDGPartialModels.SMALL_GAUGE_DIAL.get();
        if (dial == null)
            return;

        ms.pushPose();
        ms.translate(.5, .5, .5);
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-lidState.facing.toYRot() + 180));
        ms.translate(.5625f, -.375, 1.0625);
        ms.translate(-.5, -.5, -.5);
        ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(lidState.progress * -90 + 90));
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(dial),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(BasinLidBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }

    private static class LidRenderState extends BlockEntityRenderState {
        private boolean onBasin;
        private Direction facing = Direction.NORTH;
        private float progress;
    }
}
