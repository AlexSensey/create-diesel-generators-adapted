package com.jesz.createdieselgenerators.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class AnimatedSpoutCastingStation extends AnimatedKinetics {
    private List<FluidStack> fluids;

    public AnimatedSpoutCastingStation withFluids(List<FluidStack> fluids) {
        this.fluids = fluids;
        return this;
    }

    @Override
    protected void drawAnimation(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
        int scale = 23;
        blockElement(AllBlocks.SPOUT.getDefaultState()).at(xOffset, yOffset).scale(scale).submit(graphics);

        float cycle = (getAnimationTime() - offset * 8) % 30;
        float squeeze = cycle < 20 ? Mth.sin((float) (cycle / 20f * Math.PI)) : 0;
        float displacement = -3 * squeeze * 20 / 32f;

        blockElement(AllPartialModels.SPOUT_TOP).at(xOffset, yOffset).scale(scale).submit(graphics);
        blockElement(AllPartialModels.SPOUT_MIDDLE).at(xOffset, yOffset)
                .atLocal(0, displacement, 0).scale(scale).submit(graphics);
        blockElement(AllPartialModels.SPOUT_BOTTOM).at(xOffset, yOffset)
                .atLocal(0, displacement * 2, 0).scale(scale).submit(graphics);
        blockElement(AllBlocks.BASIN.getDefaultState()).at(xOffset, yOffset)
                .atLocal(0, 1.65f, 0).scale(scale).submit(graphics);

        if (fluids == null || fluids.isEmpty())
            return;
        @SuppressWarnings("unchecked")
        TypedInstance<Fluid> fluid = (TypedInstance<Fluid>) fluids.getFirst();
        float fluidScale = 16f / scale;
        float from = 3f / 16f;
        float to = 17f / 16f;
        GuiGameElement.submitFluidBox(fluid, 0, 0, 0, fluidScale,
                0, 0, 0, from, from, from, to, to, to);

        float width = squeeze * 20 / 128f;
        float streamFrom = .5f - width / 2;
        float streamTo = .5f + width / 2;
        GuiGameElement.submitFluidBox(fluid, .5f, 1.5f, .5f, fluidScale,
                -.5f, 0, -.5f, streamFrom, 0, streamFrom, streamTo, 2, streamTo);
    }
}
