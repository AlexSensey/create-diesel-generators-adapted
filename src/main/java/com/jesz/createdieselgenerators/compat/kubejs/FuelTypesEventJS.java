package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.fuel_type.FuelType;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class FuelTypesEventJS extends EventJS {
    public static Map<String, FuelType> addedTypes = new HashMap<>();

    @Override
    protected Object defaultExitValue() {
        return -1;
    }

    @Info("Adds a fuel type")
    public FuelTypeBuilder add(String fluidId) {
        return new FuelTypeBuilder(t -> addedTypes.put(fluidId, t));
    }

    @Info("Removes a fuel type")
    public void remove(String fluidId) {
        addedTypes.put(fluidId, FuelType.EMPTY);
    }
}
