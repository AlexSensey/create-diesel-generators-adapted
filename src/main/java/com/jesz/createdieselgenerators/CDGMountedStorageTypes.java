package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelMountedStorageType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorageType;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGMountedStorageTypes {
    public static final RegistryEntry<MountedFluidStorageType<?>, OilBarrelMountedStorageType> OIL_BARREL = simpleFluid("oil_barrel", OilBarrelMountedStorageType::new);

    private static <T extends MountedFluidStorageType<?>> RegistryEntry<MountedFluidStorageType<?>, T> simpleFluid(String name, Supplier<T> supplier) {
        return REGISTRATE.mountedFluidStorage(name, supplier).register();
    }


    public static void register() {};
}
