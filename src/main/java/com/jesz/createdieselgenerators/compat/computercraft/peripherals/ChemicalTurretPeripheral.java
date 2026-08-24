package com.jesz.createdieselgenerators.compat.computercraft.peripherals;

import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.Nullable;
import net.minecraft.util.Mth;

public class ChemicalTurretPeripheral implements IPeripheral {
    private final ChemicalTurretBlockEntity blockEntity;

    public ChemicalTurretPeripheral(ChemicalTurretBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public String getType() {
        return "CDG_ChemicalTurret";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }

    @LuaFunction
    public final float getHorizontalRotation(){
        return blockEntity.targetedHorizontalRotation;
    }

    @LuaFunction
    public final float getVerticalRotation(){
        return blockEntity.targetedVerticalRotation;
    }

    @LuaFunction(mainThread = true)
    public final void setHorizontalRotation(int v) {
        blockEntity.targetedHorizontalRotation = v;
        blockEntity.sync = true;
    }

    @LuaFunction(mainThread = true)
    public final void setVerticalRotation(int v) {
        blockEntity.targetedVerticalRotation = Mth.clamp(v, -50, 11);
        blockEntity.sync = true;
    }

    @LuaFunction(mainThread = true)
    public final void spray() {
        blockEntity.shootNextTick = true;
        blockEntity.sync = true;
    }
}
