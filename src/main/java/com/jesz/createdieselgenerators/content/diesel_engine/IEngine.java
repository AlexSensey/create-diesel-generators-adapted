package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface IEngine {

    default boolean enabled(){
        if(fs().isEmpty())
            return false;
        if(FuelTypeManager.fuelTypes.containsKey(fs().getFluid()))
            return !CDGConfig.ENGINES_DISABLED_WITH_REDSTONE.get() || !self().getBlockState().getValue(DieselEngineBlock.POWERED);
        return false;
    }
    default boolean validFS(){
        if(fs().isEmpty())
            return false;
        return FuelTypeManager.fuelTypes.containsKey(fs().getRawFluid());
    }
    default FluidStack fs(){
        return getTank().getPrimaryHandler().getFluid();
    }

    default float getFuelSpeed(){
        return FuelTypeManager.getGeneratedSpeed(self(), fs().getRawFluid());
    }
    default float getFuelStress(){
        return FuelTypeManager.getGeneratedStress(self(), fs().getRawFluid()) / getFuelSpeed();
    }
    default void tickFuelUsage(float multiplier){
        if(getTick() % 20 == 0){
            getTank().getPrimaryHandler().drain((int) (FuelTypeManager.getBurnRate(self(), fs().getRawFluid()) * multiplier), IFluidHandler.FluidAction.EXECUTE);
        }
    }
    default void tickFuelUsage(){
        tickFuelUsage(1);
    }
    int getTick();
    SmartBlockEntity self();
    SmartFluidTankBehaviour getTank();

    void playSound();
}
