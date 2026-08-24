package com.jesz.createdieselgenerators.content.concrete;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public class ConcreteFluid extends BaseFlowingFluid.Source {
    DyeColor color;
    public ConcreteFluid(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState blockState, FluidState state) {
        if (level.getBlockState(pos.below()).isAir()) {
            BlockState blockstate = state.createLegacyBlock();
            level.setBlockAndUpdate(pos.below(), blockstate);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else
            super.tick(level, pos, blockState, state);

    }

    @Override
    protected void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
        if (random.nextInt(30) == 0) {
            Block concrete = BuiltInRegistries.BLOCK
                    .get(Identifier.withDefaultNamespace(color.getName() + "_concrete"))
                    .map(holder -> holder.value())
                    .orElse(Blocks.AIR);
            level.setBlockAndUpdate(pos, concrete.defaultBlockState());
        }
        super.randomTick(level, pos, state, random);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }
}
