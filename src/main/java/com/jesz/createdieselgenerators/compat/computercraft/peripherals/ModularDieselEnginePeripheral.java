package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.templates.FluidTank;


public class ModularDieselEnginePeripheral extends SyncedPeripheral<ModularDieselEngineBlockEntity> {

    public ModularDieselEnginePeripheral(ModularDieselEngineBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "CDG_DieselEngine";
    }

    @LuaFunction
    public final void setMovementDirection(boolean direction){
        blockEntity.movementDirection.setValue(direction ? 1 : 0);
    }

    @LuaFunction
    public final boolean getMovementDirection(){
        return blockEntity.movementDirection.getValue() == 1;
    }

    @LuaFunction
    public final float getStressCapacity(){
        ModularDieselEngineBlockEntity frontEngine = blockEntity.controller;

        if(frontEngine == null)
            return blockEntity.calculateAddedStressCapacity();
        return frontEngine.calculateAddedStressCapacity();
    }
    @LuaFunction
    public final int getEngineMultiBlockSize(){
        return blockEntity.length;
    }
    @LuaFunction
    public final float getSpeed(){
        ModularDieselEngineBlockEntity frontEngine = blockEntity.controller;
        if(frontEngine == null)
            return Math.abs(blockEntity.getGeneratedSpeed());
        return Math.abs(frontEngine.getGeneratedSpeed());
    }

    @LuaFunction
    public final float getFuelAmount(){
        ModularDieselEngineBlockEntity frontEngine = blockEntity.controller;
        if(frontEngine == null)
            return blockEntity.tank.getPrimaryHandler().getFluid().getAmount();
        return frontEngine.tank.getPrimaryHandler().getFluid().getAmount();
    }
    @LuaFunction
    public final float getFuelBurnRate(){
        return FuelTypeManager.getBurnRate(blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(new FluidTank(1)).getFluidInTank(0).getFluid());
    }
}
