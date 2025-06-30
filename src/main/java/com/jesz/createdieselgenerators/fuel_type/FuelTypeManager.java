package com.jesz.createdieselgenerators.fuel_type;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.jesz.createdieselgenerators.compat.kubejs.CDGKubeJSPlugin;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.jesz.createdieselgenerators.packets.FuelTypesUpdatePacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.simibubi.create.AllTags.optionalTag;

public class FuelTypeManager {
    public static Map<Fluid, FuelType> fuelTypes = new HashMap<>();
    public static Map<String, FuelType> fuelTags = new HashMap<>();
    static boolean sentPacket = true;
    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new Gson();
        public static final ReloadListener INSTANCE = new ReloadListener();

        public ReloadListener() {
            super(GSON, "diesel_engine_fuel_types");
        }
        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {

            fuelTypes.clear();
            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                JsonElement element = entry.getValue();
                if (!element.isJsonObject())
                    continue;

                if (!element.getAsJsonObject().has("fluid"))
                    throw new JsonSyntaxException("Invalid Fuel Type: missing fuel values for normal engine");

                String fluidName = element.getAsJsonObject().get("fluid").getAsString();

                if (fluidName.startsWith("#")) {
                    fuelTags.put(fluidName.substring(1), FuelType.fromJSON(element));
                } else {
                    Optional<Holder.Reference<Fluid>> fluid = ForgeRegistries.FLUIDS.getDelegate(new ResourceLocation(fluidName));
                    if (fluid.isEmpty())
                        continue;
                    fuelTypes.put(fluid.get().get(), FuelType.fromJSON(element));
                }
            }

            if (ModList.get().isLoaded("kubejs"))
                CDGKubeJSPlugin.addFuels();

            sentPacket = false;
            tryPopulateTags();
        }
    }

    public static void tryPopulateTags() {
        // since the fuel types are registered before the tags, this weird bit of code is needed for it to work
        if (fuelTags.isEmpty()) {
            if (!sentPacket) {
                try {
                    CDGPackets.getChannel().send(PacketDistributor.ALL.noArg(), new FuelTypesUpdatePacket(FuelTypeManager.fuelTypes));
                } catch (NullPointerException ignored) {}
                sentPacket = true;
            }
            return;
        }
        if (ForgeRegistries.FLUIDS.tags().stream().toList().isEmpty())
            return;
        for (Map.Entry<String, FuelType> entry : Map.copyOf(fuelTags).entrySet()) {
            ForgeRegistries.FLUIDS.tags()
                .getTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation(entry.getKey())))
                .stream()
                .distinct()
                .toList().forEach(fluid ->
                    {
                        fuelTypes.put(fluid, entry.getValue());
                        fuelTags.remove(entry.getKey(), entry.getValue());
                    }
                );
        }
    }

    public static boolean isFuel(Fluid fluid) {
        tryPopulateTags();
        return fuelTypes.containsKey(fluid);
    }

    public static FuelType getType(Fluid fluid){
        return fuelTypes.get(fluid);
    }

    public static float getBurnerStrength(Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).getBurnerStrength();
        return 0;
    }

    public static float getGeneratedSpeed(BlockEntity be, Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).getGenerated(be).getFirst();
        return 0;
    }

    public static float getGeneratedStress(BlockEntity be, Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).getGenerated(be).getSecond();
        return 0;
    }

    public static float getGeneratedSpeed(EngineTypes engine, Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            if(engine == EngineTypes.NORMAL)
                return fuelTypes.get(fluid).getGeneratedNormal().getFirst();
            else if(engine == EngineTypes.MODULAR)
                return fuelTypes.get(fluid).getGeneratedModular().getFirst();
            else if(engine == EngineTypes.HUGE)
                return fuelTypes.get(fluid).getGeneratedHuge().getFirst();
        return 0;
    }

    public static float getGeneratedStress(EngineTypes engine, Fluid fluid) {
        tryPopulateTags();
        if(fuelTypes.containsKey(fluid))
            if(engine == EngineTypes.NORMAL)
                return fuelTypes.get(fluid).getGeneratedNormal().getSecond();
            else if(engine == EngineTypes.MODULAR)
                return fuelTypes.get(fluid).getGeneratedModular().getSecond();
            else if(engine == EngineTypes.HUGE)
                return fuelTypes.get(fluid).getGeneratedHuge().getSecond();
        return 0;
    }

    public static float getBurnRate(EngineTypes engine, Fluid fluid) {
        tryPopulateTags();
        if(fuelTypes.containsKey(fluid))
            if(engine == EngineTypes.NORMAL)
                return fuelTypes.get(fluid).normalBurn();
            else if(engine == EngineTypes.MODULAR)
                return fuelTypes.get(fluid).modularBurn();
            else if(engine == EngineTypes.HUGE)
                return fuelTypes.get(fluid).hugeBurn();
        return 0;
    }
    public static float getGeneratedSpeed(Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).getGeneratedNormal().getFirst();
        return 0;
    }
    public static float getGeneratedStress(Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).getGeneratedNormal().getSecond();
        return 0;
    }
    public static float getBurnRate(BlockEntity be, Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).getBurn(be);
        return 0;
    }
    public static float getBurnRate(Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).normalBurn();
        return 0;
    }
    public static float getSoundPitch(Fluid fluid) {
        tryPopulateTags();
        if (fuelTypes.containsKey(fluid))
            return fuelTypes.get(fluid).soundPitch();
        return 1;
    }
}
