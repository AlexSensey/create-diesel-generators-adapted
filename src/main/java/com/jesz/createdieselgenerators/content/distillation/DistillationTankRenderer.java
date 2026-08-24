package com.jesz.createdieselgenerators.content.distillation;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.api.data.Iterate;
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
import net.minecraft.core.TypedInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class DistillationTankRenderer extends SafeBlockEntityRenderer<DistillationTankBlockEntity> {
    public DistillationTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(DistillationTankBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new DistillationRenderState();
    }

    @Override
    public void extractRenderState(DistillationTankBlockEntity be, BlockEntityRenderState state,
                                   float partialTicks, Vec3 cameraPos,
                                   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (!(state instanceof DistillationRenderState tankState))
            return;

        tankState.controller = be.isController();
        tankState.bottom = be.isBottom();
        tankState.width = be.width;
        tankState.height = be.height;
        tankState.fluid = be.tankInventory.getFluid().copy();
        tankState.partialTicks = partialTicks;
        LerpedFloat fluidLevel = be.getFluidLevel();
        tankState.level = fluidLevel == null ? 0 : fluidLevel.getValue(partialTicks);
        tankState.progress = Mth.clamp(be.currentRecipe == null ? be.progress
                : (be.processingTime - partialTicks) / be.currentRecipe.getProcessingDuration(), 0, 1);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof DistillationRenderState tankState) || !tankState.controller)
            return;

        if (tankState.bottom)
            submitGauges(tankState, ms, collector, state.lightCoords);
        submitFluid(tankState, ms, collector, state.lightCoords);
    }

    private static void submitGauges(DistillationRenderState state, PoseStack ms,
                                     SubmitNodeCollector collector, int light) {
        BlockStateModelPart gauge = CDGPartialModels.DISTILLATION_GAUGE.get();
        BlockStateModelPart dial = AllPartialModels.BOILER_GAUGE_DIAL.get();
        if (gauge == null || dial == null)
            return;

        float dialPivotY = 6f / 16;
        float dialPivotZ = 8f / 16;
        ms.pushPose();
        ms.translate(state.width / 2f, .5f, state.width / 2f);

        for (Direction direction : Iterate.horizontalDirections) {
            ms.pushPose();
            ms.mulPose(Axis.YP.rotationDegrees(-direction.toYRot() - 90));
            ms.translate(-.5f, -.5f, -.5f);
            ms.translate(state.width / 2f - 6 / 16f, 0, 0);
            collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(gauge),
                    BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
            ms.translate(0, dialPivotY, dialPivotZ);
            ms.mulPose(Axis.XP.rotationDegrees(-145 * state.progress + 90));
            ms.translate(0, -dialPivotY, -dialPivotZ);
            collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(dial),
                    BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
            ms.popPose();
        }

        ms.popPose();
    }

    private static void submitFluid(DistillationRenderState state, PoseStack ms,
                                    SubmitNodeCollector collector, int light) {
        float capHeight = .25f;
        float tankHullWidth = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = state.height - 2 * capHeight - minPuddleHeight;
        if (totalHeight <= 0)
            return;

        float level = state.level;
        if (level < 1 / (512f * totalHeight))
            return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        FluidStack fluid = state.fluid;
        if (fluid.isEmpty())
            return;

        float xMin = tankHullWidth;
        float xMax = xMin + state.width - 2 * tankHullWidth;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;
        if (fluid.getFluid().getFluidType().isLighterThanAir()) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }
        float zMin = tankHullWidth;
        float zMax = zMin + state.width - 2 * tankHullWidth;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);
        FluidRenderHelper.submitFluidBox(collector, (TypedInstance<Fluid>) fluid,
                xMin, yMin, zMin, xMax, yMax, zMax, ms, light, false, true);
        ms.popPose();
    }

    public boolean shouldRenderOffScreen(DistillationTankBlockEntity be) {
        return be.isController();
    }

    private static class DistillationRenderState extends BlockEntityRenderState {
        private boolean controller;
        private boolean bottom;
        private int width;
        private int height;
        private float level;
        private float progress;
        @SuppressWarnings("unused")
        private float partialTicks;
        private FluidStack fluid = FluidStack.EMPTY;
    }
}
