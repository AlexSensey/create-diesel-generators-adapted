package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class FuelTypesEventJS extends EventJS {

    @Override
    protected Object defaultExitValue() {
        return -1;
    }

    @Info("Adds a fuel type")
    public FuelTypeBuilder add(String fluidId){
        return new FuelTypeBuilder(t -> {
            if(fluidId.startsWith("#")){
                FuelTypeManager.fuelTags.put(fluidId.substring(1), t);
                FuelTypeManager.tryPopulateTags();
            }else{
                Optional<Holder.Reference<Fluid>> fluid = ForgeRegistries.FLUIDS.getDelegate(new ResourceLocation(fluidId));
                if(fluid.isEmpty())
                    return;
                FuelTypeManager.fuelTypes.put(fluid.get().get(), t);
            }
        });
    }
    @Info("Removes a fuel type")
    public void remove(String fluidId){
        if(fluidId.startsWith("#")){
            FuelTypeManager.fuelTags.remove(fluidId.substring(1));
        }else{
            Optional<Holder.Reference<Fluid>> fluid = ForgeRegistries.FLUIDS.getDelegate(new ResourceLocation(fluidId));
            if(fluid.isEmpty())
                return;
            FuelTypeManager.fuelTypes.remove(fluid.get().get());
        }
    }
}
