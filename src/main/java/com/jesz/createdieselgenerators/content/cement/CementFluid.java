package com.jesz.createdieselgenerators.content.cement;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

public class CementFluid extends ForgeFlowingFluid.Source {
    DyeColor color;
    public CementFluid(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.setBlockAndUpdate(pos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation(color.getName() + "_concrete")).defaultBlockState());
        }
        super.randomTick(level, pos, state, random);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }
}
