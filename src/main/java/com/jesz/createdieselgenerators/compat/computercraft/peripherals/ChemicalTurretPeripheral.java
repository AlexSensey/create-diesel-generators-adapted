package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.util.Mth;

public class ChemicalTurretPeripheral extends SyncedPeripheral<ChemicalTurretBlockEntity> {
    public ChemicalTurretPeripheral(ChemicalTurretBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "CDG_ChemicalTurret";
    }

    @LuaFunction
    public final float getHorizontalRotation(){
        return blockEntity.targetedHorizontalRotation;
    }

    @LuaFunction
    public final float getVerticalRotation(){
        return blockEntity.targetedVerticalRotation;
    }

    @LuaFunction
    public final void setHorizontalRotation(int v) {
        blockEntity.targetedHorizontalRotation = v;
        blockEntity.sync = true;
    }
    @LuaFunction
    public final void setVerticalRotation(int v) {
        blockEntity.targetedVerticalRotation = Mth.clamp(v, -50, 11);
        blockEntity.sync = true;
    }
    @LuaFunction
    public final void spray() {
        blockEntity.shootNextTick = true;
        blockEntity.sync = true;
    }
}
