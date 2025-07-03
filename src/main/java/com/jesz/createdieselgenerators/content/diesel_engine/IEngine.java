package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public interface IEngine {

    default boolean enabled() {
        if (validFS())
            return !(CDGConfig.ENGINES_DISABLED_WITH_REDSTONE.get() && self().getBlockState().getValue(DieselEngineBlock.POWERED));
        return false;
    }

    default boolean validFS() {
        if (fs().isEmpty())
            return false;
        return FuelTypeManager.isFuel(fs().getFluid());
    }

    default FluidStack fs() {
        return getTank().getFluid();
    }

    default float getFuelSpeed() {
        return FuelTypeManager.getGeneratedSpeed(self(), fs().getFluid());
    }

    default float getFuelCapacity() {
        float speed = getFuelSpeed();
        if (speed == 0)
            return speed;
        return FuelTypeManager.getGeneratedStress(self(), fs().getFluid()) / speed;
    }

    default float getFuelBurnRate() {
        return FuelTypeManager.getBurnRate(self(), fs().getFluid());
    }

    default float getFuelSoundPitch() {
        return FuelTypeManager.getSoundPitch(fs().getFluid());
    }

    float getRemainingTicks();

    SmartBlockEntity self();

    FluidTank getTank();

    EngineUpgrades getUpgrade();
    void setUpgrade(EngineUpgrades upgrade);
}
