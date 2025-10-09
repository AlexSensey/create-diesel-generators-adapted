package com.jesz.createdieselgenerators.compat.computercraft;

import com.jesz.createdieselgenerators.compat.computercraft.peripherals.ChemicalTurretPeripheral;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class CDGComputerBehaviour extends AbstractComputerBehaviour {
    IPeripheral peripheral;
    Supplier<IPeripheral> peripheralSupplier;
    SmartBlockEntity be;

    public CDGComputerBehaviour(SmartBlockEntity be) {
        super(be);
        this.peripheralSupplier = getPeripheralFor(be);
        this.be = be;
    }

    public static Supplier<IPeripheral> getPeripheralFor(SmartBlockEntity be) {
        if (be instanceof ChemicalTurretBlockEntity ctbe)
            return () -> new ChemicalTurretPeripheral(ctbe);

        throw new IllegalArgumentException(
                "No peripheral available for " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()));
    }

    @Override
    public IPeripheral getPeripheralCapability() {
        if (peripheral == null)
            peripheral = peripheralSupplier.get();
        return peripheral;
    }

    @Override
    public void removePeripheral() {
        if (peripheral != null)
            getWorld().invalidateCapabilities(be.getBlockPos());
    }
}
