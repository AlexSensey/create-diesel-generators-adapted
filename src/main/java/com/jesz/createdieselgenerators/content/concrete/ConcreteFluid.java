package com.jesz.createdieselgenerators.content.concrete;

import com.jesz.createdieselgenerators.CDGFluids;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.Map;

public class ConcreteFluid extends BaseFlowingFluid.Source {
    DyeColor color;
    public ConcreteFluid(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState state) {
        if (level.getBlockState(pos.below()).isAir()) {
            BlockState blockstate = state.createLegacyBlock();
            level.setBlockAndUpdate(pos.below(), blockstate);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else
            super.tick(level, pos, state);

    }

    @Override
    protected void randomTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        if (random.nextInt(30) == 0) {
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(color.getName() + "_concrete")).defaultBlockState());
        }
        super.randomTick(level, pos, state, random);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }
}
