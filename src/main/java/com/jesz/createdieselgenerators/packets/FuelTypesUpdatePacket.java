package com.jesz.createdieselgenerators.packets;

import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FuelTypesUpdatePacket extends SimplePacketBase {
    Map<Fluid, FuelType> allTypes;
    public FuelTypesUpdatePacket(Map<Fluid, FuelType> allTypes){
        this.allTypes = Map.copyOf(allTypes);
    }
    public FuelTypesUpdatePacket(FriendlyByteBuf buffer){
        allTypes = new HashMap<>();

        buffer.readCollection(ArrayList::new, b -> {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(buffer.readResourceLocation());
            FuelType type = new FuelType(
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readFloat()
            );
            allTypes.put(fluid, type);
            return null;
        });
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(allTypes.entrySet(), (b, e) -> {
            Fluid fluid = e.getKey();
            FuelType type = e.getValue();
            buffer.writeResourceLocation(ForgeRegistries.FLUIDS.getKey(fluid));

            buffer.writeFloat(type.normalSpeed());
            buffer.writeFloat(type.normalStrength());
            buffer.writeFloat(type.normalBurn());
            buffer.writeFloat(type.modularSpeed());
            buffer.writeFloat(type.modularStrength());
            buffer.writeFloat(type.modularBurn());
            buffer.writeFloat(type.hugeSpeed());
            buffer.writeFloat(type.hugeStrength());
            buffer.writeFloat(type.hugeBurn());
            buffer.writeFloat(type.soundPitch());
            buffer.writeFloat(type.burnerStrength());
        });
    }


    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            FuelTypeManager.fuelTypes = allTypes;
        });
        return true;
    }
}
