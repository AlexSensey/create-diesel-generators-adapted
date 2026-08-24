package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;

public class AnimatedDistillationTower extends AnimatedKinetics {

    @Override
    protected void drawAnimation(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
        draw(graphics, xOffset, yOffset, 3);
    }
    public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset, int height) {
        int scale = 23;

        blockElement(CDGPartialModels.JEI_DISTILLER_BOTTOM)
                .atLocal(0, 1, 0).at(xOffset, yOffset)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .submit(graphics);
        for (int i = 0; i < height-1; i++) {
            blockElement(CDGPartialModels.JEI_DISTILLER_MIDDLE)
                    .atLocal(0, -i, 0).at(xOffset, yOffset)
                    .rotateBlock(0, 90, 0)
                    .scale(scale)
                    .submit(graphics);
        }
        blockElement(CDGPartialModels.JEI_DISTILLER_TOP)
                .atLocal(0, -height+1, 0).at(xOffset, yOffset)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .submit(graphics);
        blockElement(CDGPartialModels.DISTILLATION_GAUGE).atLocal(1, 1, 0.125f).at(xOffset, yOffset).rotate(0, -90, 0).scale(scale).submit(graphics);
        blockElement(CDGPartialModels.DISTILLATION_GAUGE).atLocal(1-0.125f, 1, 1).at(xOffset, yOffset).rotate(0, 180, 0).scale(scale).submit(graphics);
    }
}
