package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ChemicalTurretRenderer extends KineticBlockEntityRenderer<ChemicalTurretBlockEntity> {
    public ChemicalTurretRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof KineticRenderState kineticState)
                || !(kineticState.blockEntity instanceof ChemicalTurretBlockEntity be)
                || isInvalid(be))
            return;

        // Restore the four side-mounted entity-filter settings from the
        // original renderer through Minecraft 26.2's lit item submit path.
        FilteringRenderer.submitOnBlockEntity(be, ms, collector, state.lightCoords);

        BlockStateModelPart largeCog = CDGPartialModels.CHEMICAL_TURRET_COG.get();
        BlockStateModelPart connector = CDGPartialModels.CHEMICAL_TURRET_CONNECTOR.get();
        BlockStateModelPart body = CDGPartialModels.CHEMICAL_TURRET_BODY.get();
        BlockStateModelPart lighter = CDGPartialModels.CHEMICAL_TURRET_LIGHTER.get();
        BlockStateModelPart smallCog = CDGPartialModels.CHEMICAL_TURRET_SMALL_COG.get();

        if (largeCog != null) {
            ms.pushPose();
            transformRotatingModel(be, ms, kineticState.partialTicks);
            submitPart(largeCog, state, ms, collector);
            ms.popPose();
        }

        float horizontalRotation = AngleHelper.angleLerp(kineticState.partialTicks,
                be.oldHorizontalRotation, be.horizontalRotation);
        float verticalRotation = AngleHelper.angleLerp(kineticState.partialTicks,
                be.oldVerticalRotation, be.verticalRotation);
        float cogRotation = Mth.lerp(kineticState.partialTicks, be.lastCogRotation, be.cogRotation);

        if (connector != null) {
            ms.pushPose();
            rotateAroundCenter(ms, horizontalRotation);
            submitPart(connector, state, ms, collector);
            ms.popPose();
        }

        if (body != null)
            submitAimingPart(body, horizontalRotation, verticalRotation, 0, state, ms, collector);
        if (be.lighterUpgrade && lighter != null)
            submitAimingPart(lighter, horizontalRotation, verticalRotation, 0, state, ms, collector);
        if (smallCog != null)
            submitAimingPart(smallCog, horizontalRotation, verticalRotation, cogRotation, state, ms, collector);
    }

    private static void submitAimingPart(BlockStateModelPart part, float horizontalRotation,
                                         float verticalRotation, float cogRotation,
                                         BlockEntityRenderState state, PoseStack ms,
                                         SubmitNodeCollector collector) {
        ms.pushPose();
        rotateAroundCenter(ms, horizontalRotation + 180);
        ms.translate(.5, 1.3125, .125);
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(verticalRotation));
        if (cogRotation != 0)
            ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(cogRotation));
        submitPart(part, state, ms, collector);
        ms.popPose();
    }

    private static void rotateAroundCenter(PoseStack ms, float degrees) {
        ms.translate(.5, .5, .5);
        ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(degrees));
        ms.translate(-.5, -.5, -.5);
    }

    private static void submitPart(BlockStateModelPart part, BlockEntityRenderState state,
                                   PoseStack ms, SubmitNodeCollector collector) {
        collector.submitBlockModel(ms, RenderTypes.solidMovingBlock(), List.of(part),
                BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    protected void renderSafe(ChemicalTurretBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // Minecraft 26.2 renders block entities through submit().
    }

    protected SuperByteBuffer getRotatedModel(ChemicalTurretBlockEntity be, BlockState state) {
        return com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility.partial(CDGPartialModels.CHEMICAL_TURRET_COG, state);
    }
}
