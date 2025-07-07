package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class CDGRegistries {
    public static final ResourceKey<Registry<FuelType>> FUEL_TYPE = key("fuel_type");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(CreateDieselGenerators.rl(name));
    }
}
