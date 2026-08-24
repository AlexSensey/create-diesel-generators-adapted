package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.item.SmartInventory;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.data.Iterate;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BulkFermenterRenderer extends SafeBlockEntityRenderer<BulkFermenterBlockEntity> {
    public BulkFermenterRenderer(BlockEntityRendererProvider.Context context){}

    @Override
    public BlockEntityRenderState createRenderState() {
        return new FermenterRenderState();
    }

    @Override
    public void extractRenderState(BulkFermenterBlockEntity be, BlockEntityRenderState state, float partialTicks,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!(state instanceof FermenterRenderState fermenterState))
            return;
        fermenterState.controller = be.isController();
        fermenterState.width = be.getWidth();
        fermenterState.progress = be.currentRecipe == null ? 0
                : (float) Mth.clamp(Mth.lerp(partialTicks,
                be.processingTime + Math.sqrt(be.width * be.height), be.processingTime)
                / be.currentRecipe.getProcessingDuration(), 0, 1);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof FermenterRenderState fermenterState) || !fermenterState.controller)
            return;
        BlockStateModelPart gauge = CDGPartialModels.BULK_FERMENTER_GAUGE.get();
        BlockStateModelPart dial = AllPartialModels.BOILER_GAUGE_DIAL.get();
        if (gauge == null || dial == null)
            return;

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        ms.pushPose();
        ms.translate(fermenterState.width / 2f, .5, fermenterState.width / 2f);
        for (Direction direction : Iterate.horizontalDirections) {
            ms.pushPose();
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(direction.toYRot()));
            ms.translate(-.5, -.5, -.5);
            ms.translate(fermenterState.width / 2f - 6 / 16f, 0, 0);
            collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(gauge),
                    BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            ms.translate(0, dialPivotY, dialPivotZ);
            ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-180 * fermenterState.progress + 90));
            ms.translate(0, -dialPivotY, -dialPivotZ);
            collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(dial),
                    BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }
        ms.popPose();
    }

    @Override
    protected void renderSafe(BulkFermenterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }

    private static class FermenterRenderState extends BlockEntityRenderState {
        private boolean controller;
        private int width;
        private float progress;
    }

}
