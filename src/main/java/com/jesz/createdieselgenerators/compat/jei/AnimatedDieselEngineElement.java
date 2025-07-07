package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.List;

public class AnimatedDieselEngineElement extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        byte enginesEnabled = (byte) ((EngineTypes.NORMAL.enabled() ? 1 : 0) + (EngineTypes.MODULAR.enabled() ? 1 : 0) + (EngineTypes.HUGE.enabled() ? 1 : 0));
        int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
        List<EngineTypes> enabledEngines = Arrays.stream(EngineTypes.values()).filter(EngineTypes::enabled).toList();
        EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 1000);

        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f + 90));
        int scale = 25;
        if (currentEngine == EngineTypes.HUGE)
            scale = 17;

        blockElement(shaft(Direction.Axis.X)).atLocal(0, currentEngine == EngineTypes.HUGE ? -1.25 : 0, 0)
                .rotateBlock(-getCurrentAngle() * 6, 0, 0)
                .scale(scale)
                .render(graphics);
        int angle = (int) (getCurrentAngle() * 18 % 360)/36;

        if (currentEngine == EngineTypes.HUGE) {
            blockElement(CDGPartialModels.ENGINE_PISTON_CONNECTOR).atLocal(0, -1.25, 0)
                    .rotateBlock(-getCurrentAngle() * 6, 0, 0)
                    .scale(scale)
                    .render(graphics);
            blockElement(CDGPartialModels.ENGINE_PISTON_LINKAGE).atLocal(0, Math.cos(getCurrentAngle()/30*Math.PI)/5 - 1, 0)
                    .scale(scale)
                    .render(graphics);
            blockElement(CDGPartialModels.JEI_ENGINE_PISTON).atLocal(0, Math.cos(getCurrentAngle()/30*Math.PI)/5, 0)
                    .scale(scale)
                    .render(graphics);
        }
        if (currentEngine == EngineTypes.NORMAL) {
            blockElement(angle == 10? CDGPartialModels.ENGINE_PISTONS_0 :
                    angle == 9 ? CDGPartialModels.ENGINE_PISTONS_1 :
                    angle == 8 ? CDGPartialModels.ENGINE_PISTONS_2 :
                    angle == 7 ? CDGPartialModels.ENGINE_PISTONS_3 :
                    angle == 6 ? CDGPartialModels.ENGINE_PISTONS_4 :
                    angle == 5 ? CDGPartialModels.ENGINE_PISTONS_4 :
                    angle == 4 ? CDGPartialModels.ENGINE_PISTONS_3 :
                    angle == 3 ? CDGPartialModels.ENGINE_PISTONS_2 :
                    angle == 2 ? CDGPartialModels.ENGINE_PISTONS_1 :
                            CDGPartialModels.ENGINE_PISTONS_0)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);
        } else if(currentEngine == EngineTypes.MODULAR) {
            blockElement(angle == 10? CDGPartialModels.MODULAR_ENGINE_PISTONS_0 :
                    angle == 9 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_1 :
                    angle == 8 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_2 :
                    angle == 7 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_3 :
                    angle == 6 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_4 :
                    angle == 5 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_4 :
                    angle == 4 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_3 :
                    angle == 3 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_2 :
                    angle == 2 ? CDGPartialModels.MODULAR_ENGINE_PISTONS_1 :
                            CDGPartialModels.MODULAR_ENGINE_PISTONS_0)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);
        }

        blockElement(currentEngine == EngineTypes.MODULAR ? CDGBlocks.MODULAR_DIESEL_ENGINE.getDefaultState() :
                currentEngine == EngineTypes.HUGE ? CDGBlocks.HUGE_DIESEL_ENGINE.getDefaultState().setValue(HugeDieselEngineBlock.FACING, Direction.UP) :
                        CDGBlocks.DIESEL_ENGINE.getDefaultState())
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }
}
