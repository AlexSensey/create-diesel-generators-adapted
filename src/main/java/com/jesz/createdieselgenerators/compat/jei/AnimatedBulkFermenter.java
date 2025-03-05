package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.client.gui.GuiGraphics;

public class AnimatedBulkFermenter extends AnimatedKinetics {
    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 201);

        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        blockElement(CDGPartialModels.JEI_BULK_FERMENTER)
                .atLocal(0, 1, 0)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);
        blockElement(CDGPartialModels.BULK_FERMENTER_GAUGE)
                .atLocal(0,2,0.125)
                .rotateBlock(0, -90, 0)
                .scale(scale)
                .render(graphics);
        blockElement(AllPartialModels.BOILER_GAUGE_DIAL)
                .atLocal(0,2,0.125)
                .rotateBlock(0, -90, 0)
                .scale(scale)
                .render(graphics);
        matrixStack.popPose();
    }
}
