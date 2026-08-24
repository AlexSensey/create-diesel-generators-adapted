package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import static com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock.ON_A_BASIN;

public class BasinFermentingStationElement extends AnimatedKinetics {

    @Override
    protected void drawAnimation(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
        int scale = 23;

        GuiGameElement.of(CDGBlocks.BASIN_LID.getDefaultState().setValue(ON_A_BASIN, true))
                .atLocal(0, 0, 0)
                .at(xOffset, yOffset)
                .scale(scale)
                .submit(graphics);

        GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
                .atLocal(0, 1, 0)
                .at(xOffset, yOffset)
                .scale(scale)
                .submit(graphics);
        blockElement(CDGPartialModels.SMALL_GAUGE_DIAL).atLocal(0.5625f, 0.375f, 0.5625f).at(xOffset, yOffset)
                .scale(scale)
                .rotate(0, 0, getCurrentAngle()/4)
                .submit(graphics);
    }
}
