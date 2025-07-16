package com.jesz.createdieselgenerators.content.concrete;

import com.jesz.createdieselgenerators.CDGFluids;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
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

    public static DyeColor getColor(Fluid fluid) {
        for (Map.Entry<DyeColor, FluidEntry<BaseFlowingFluid.Flowing>> e : CDGFluids.CONCRETE.entrySet())
            if (fluid.isSame(e.getValue().get()))
                return e.getKey();
        return null;
    }
}
