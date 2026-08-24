package com.jesz.createdieselgenerators.content.pumpjack;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.model.CreateStandaloneModels;

import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

/** Renders only the moving adhesive face; the Pumpjack block model owns its body. */
public class NoShaftBearingRenderer<T extends KineticBlockEntity & IBearingBlockEntity>
        extends KineticBlockEntityRenderer<T> {
    private List<BlockStateModelPart> bearingTopModel;
    private List<BlockStateModelPart> woodenBearingTopModel;

    public NoShaftBearingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack ms, SubmitNodeCollector collector,
                       CameraRenderState cameraRenderState) {
        if (!(state instanceof KineticRenderState kineticState))
            return;
        if (!(kineticState.blockEntity instanceof KineticBlockEntity kineticBE))
            return;
        if (!(kineticBE instanceof IBearingBlockEntity bearingBE))
            return;

        @SuppressWarnings("unchecked")
        T be = (T) kineticBE;
        if (isInvalid(be))
            return;

        List<BlockStateModelPart> top = getTopModel(bearingBE.isWoodenTop());
        if (top.isEmpty())
            return;

        Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING);
        float angle = bearingBE.getInterpolatedAngle(kineticState.partialTicks - 1) / 180f * (float) Math.PI;

        ms.pushPose();
        ms.translate(.5, .5, .5);
        ms.mulPose(rotation(facing.getAxis(), angle));
        if (facing.getAxis().isHorizontal())
            ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite())));
        ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
        ms.translate(-.5, -.5, -.5);
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), top, BlockModelRenderState.EMPTY_TINTS,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        // Minecraft 26.2 submits this model through submit().
    }

    private List<BlockStateModelPart> getTopModel(boolean wooden) {
        if (wooden) {
            if (woodenBearingTopModel == null)
                woodenBearingTopModel = getModel(CreateStandaloneModels.BEARING_TOP_WOODEN);
            return woodenBearingTopModel;
        }
        if (bearingTopModel == null)
            bearingTopModel = getModel(CreateStandaloneModels.BEARING_TOP);
        return bearingTopModel;
    }

    private static List<BlockStateModelPart> getModel(StandaloneModelKey<BlockStateModelPart> key) {
        BlockStateModelPart model = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
        return model == null ? List.of() : List.of(model);
    }
}
