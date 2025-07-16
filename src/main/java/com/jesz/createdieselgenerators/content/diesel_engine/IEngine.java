package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public interface IEngine {

    default boolean enabled() {
        if (validFS())
            return !(CDGConfig.ENGINES_DISABLED_WITH_REDSTONE.get() && self().getBlockState().getValue(DieselEngineBlock.POWERED));
        return false;
    }

    default boolean validFS() {
        if (fs().isEmpty())
            return false;
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fs().getFluid()) != FuelType.EMPTY;
    }

    default FluidStack fs() {
        return getTank().getFluid();
    }

    default float getFuelSpeed() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fs().getFluid()).getGenerated(self()).speed();
    }

    default float getFuelCapacity() {
        float speed = getFuelSpeed();
        if (speed == 0)
            return speed;
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fs().getFluid()).getGenerated(self()).strength() / speed;
    }

    default float getFuelBurnRate() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fs().getFluid()).getGenerated(self()).burn();
    }

    default float getFuelSoundPitch() {
        return FuelType.getTypeFor(self().getLevel().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fs().getFluid()).soundPitch();
    }

    float getRemainingTicks();

    SmartBlockEntity self();

    FluidTank getTank();

    EngineUpgrades getUpgrade();
    void setUpgrade(EngineUpgrades upgrade);
}
