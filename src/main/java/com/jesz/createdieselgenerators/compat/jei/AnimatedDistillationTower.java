package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;

public class AnimatedDistillationTower extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        draw(graphics, xOffset, yOffset, 3);
    }
    public void draw(GuiGraphics graphics, int xOffset, int yOffset, int height) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 201);

        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        blockElement(CDGPartialModels.JEI_DISTILLER_BOTTOM)
                .atLocal(0, 1, 0)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);
        for (int i = 0; i < height-1; i++) {
            blockElement(CDGPartialModels.JEI_DISTILLER_MIDDLE)
                    .atLocal(0, -i, 0)
                    .rotateBlock(0, 90, 0)
                    .scale(scale)
                    .render(graphics);
        }
        blockElement(CDGPartialModels.JEI_DISTILLER_TOP)
                .atLocal(0, -height+1, 0)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);
        blockElement(CDGPartialModels.DISTILLATION_GAUGE).atLocal(1, 1, 0.125).rotate(0, -90, 0).scale(scale).render(graphics);
        blockElement(CDGPartialModels.DISTILLATION_GAUGE).atLocal(1-0.125, 1, 1).rotate(0, 180, 0).scale(scale).render(graphics);
        matrixStack.popPose();
    }
}