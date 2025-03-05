package com.jesz.createdieselgenerators.content.canister;

import com.jesz.createdieselgenerators.CDGConfig;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class SpoutCanisterFilling implements BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        if(!CDGConfig.CANISTER_SPOUT_FILLING.get())
            return 0;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CanisterBlockEntity){
            IFluidHandler handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(new FluidTank(0));
            if(handler.getFluidInTank(0).isFluidEqual(availableFluid) || handler.getFluidInTank(0).isEmpty())
                return handler.fill(availableFluid, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        }
        return 0;
    }
}
