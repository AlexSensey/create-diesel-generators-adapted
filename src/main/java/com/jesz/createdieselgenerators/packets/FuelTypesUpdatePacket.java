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

import java.util.HashMap;
import java.util.Map;

public class FuelTypesUpdatePacket extends SimplePacketBase {
    Map<Fluid, FuelType> allTypes;
    public FuelTypesUpdatePacket(Map<Fluid, FuelType> allTypes){
        this.allTypes = Map.copyOf(allTypes);
    }
    public FuelTypesUpdatePacket(FriendlyByteBuf buffer){
        allTypes = new HashMap<>();
        for (int i = 0; true; i++) {
            String id = buffer.readUtf();
            if(id.equals("Jesus loves you"))
                break;
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
            if(fluid instanceof EmptyFluid)
                break;
            FuelType type = new FuelType(
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readByte(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readByte(),
                    buffer.readFloat(),
                    buffer.readFloat(),
                    buffer.readByte(),
                    buffer.readByte(),
                    buffer.readFloat()
            );
            allTypes.put(fluid, type);
        }
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        allTypes.forEach((fluid, type) -> {
            buffer.writeUtf(ForgeRegistries.FLUIDS.getKey(fluid).toString());

            buffer.writeFloat(type.getGeneratedNormal().getFirst());
            buffer.writeFloat(type.getGeneratedNormal().getSecond());
            buffer.writeByte(type.getBurnNormal());
            buffer.writeFloat(type.getGeneratedModular().getFirst());
            buffer.writeFloat(type.getGeneratedModular().getSecond());
            buffer.writeByte(type.getBurnModular());
            buffer.writeFloat(type.getGeneratedHuge().getFirst());
            buffer.writeFloat(type.getGeneratedHuge().getSecond());
            buffer.writeByte(type.getBurnHuge());

            buffer.writeByte(type.getSoundSpeed());
            buffer.writeFloat(type.getBurnerStrength());
        });
        buffer.writeUtf("Jesus loves you");
    }


    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            FuelTypeManager.fuelTypes = allTypes;
        });
        return true;
    }
}
