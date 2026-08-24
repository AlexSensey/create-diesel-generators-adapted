package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AnimatedBulkFermenter extends AnimatedKinetics {
    @Override
    protected void drawAnimation(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
        int scale = 23;

        blockElement(CDGPartialModels.JEI_BULK_FERMENTER)
                .atLocal(0, 1, 0)
                .at(xOffset, yOffset)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .submit(graphics);
        blockElement(CDGPartialModels.BULK_FERMENTER_GAUGE)
                .atLocal(0,2,0.125f).at(xOffset, yOffset)
                .rotateBlock(0, -90, 0)
                .scale(scale)
                .submit(graphics);
        blockElement(AllPartialModels.BOILER_GAUGE_DIAL)
                .atLocal(0,2,0.125f).at(xOffset, yOffset)
                .rotateBlock(0, -90, 0)
                .scale(scale)
                .submit(graphics);
    }
}
