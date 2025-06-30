package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.util.Objects;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;

public class ModularDieselEngineRenderer extends ShaftRenderer<ModularDieselEngineBlockEntity> {

    public ModularDieselEngineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ModularDieselEngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        int angle = (int) (Math.abs(KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be))*180/Math.PI) * 3 % 360)/36;

        ModularDieselEngineBlockEntity controller = be.getControllerBE();
        Objects.requireNonNullElse(controller, be).upgrade.render(be, partialTicks, ms, buffer, light);

        CachedBuffers.partial( angle == 10? CDGPartialModels.MODULAR_ENGINE_PISTONS_0 :
                                angle == 9 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_1 :
                                angle == 8 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_2 :
                                angle == 7 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_3 :
                                angle == 6 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_4 :
                                angle == 5 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_4 :
                                angle == 4 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_3 :
                                angle == 3 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_2 :
                                angle == 2 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_1 :
                                        CDGPartialModels.MODULAR_ENGINE_PISTONS_0
                            , be.getBlockState()).center()
                    .rotateYDegrees(be.getBlockState().getValue(FACING).toYRot()).uncenter()
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));


        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

}
