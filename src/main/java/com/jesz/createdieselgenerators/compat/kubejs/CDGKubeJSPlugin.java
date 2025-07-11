package com.jesz.createdieselgenerators.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CDGKubeJSPlugin extends KubeJSPlugin {
    public static EventGroup GROUP = EventGroup.of("CDGEvents");
    public static EventHandler MOLDS = GROUP.startup("molds", () -> MoldEventJS.class);
    public static EventHandler OIL_CHUNKS = GROUP.server("oilAmount", () -> GetChunkOilAmountEventJS.class);
    public static EventHandler FUEL_TYPES = GROUP.server("fuelTypes", () -> FuelTypesEventJS.class);
    public static EventHandler LIGHTER_SKINS = GROUP.client("lighterSkins", () -> LighterSkinsEventJS.class);

    static {
        OIL_CHUNKS.hasResult();
    }

    @Override
    public void registerEvents() {
        GROUP.register();
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        filter.allow("com.jesz.createdieselgenerators");
        filter.deny("com.jesz.createdieselgenerators.mixins");
        filter.deny(CDGKubeJSPlugin.class);
    }
    public static void registerMolds(){
        MoldEventJS event = new MoldEventJS();
        MOLDS.post(event);
    }

    @Override
    public void initStartup() {
        registerMolds();
    }

    public static int calculateOilChunks(List<Holder<Biome>> biomes, ChunkPos chunkPos, long seed) {
        if(!OIL_CHUNKS.hasListeners())
            return -1;

        GetChunkOilAmountEventJS event = new GetChunkOilAmountEventJS();
        event.chunkPos = chunkPos;
        event.seed = seed;
        String[] stringBiomes = new String[biomes.size()];
        for (int i = 0; i < stringBiomes.length; i++) {
            stringBiomes[i] = biomes.stream().map(b -> ((Holder.Reference)b).key().location().toString()).toList().get(i);
        }
        event.biomes = stringBiomes;

        return ((Double)OIL_CHUNKS.post(event).value()).intValue();
    }

    public static void registerLighterSkins() {
        LighterSkinsEventJS event = new LighterSkinsEventJS();
        LIGHTER_SKINS.post(event);
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        CDGKubeJSPlugin.registerLighterSkins();

        MoldEventJS.addedMolds.forEach((rl, name) -> {
            generator.json(new ResourceLocation(rl.getNamespace(), "models/item/mold/"+rl.getPath()), generateTextureModel(new ResourceLocation(rl.getNamespace(), "item/mold/"+rl.getPath())));
        });

        LighterSkinsEventJS.addedIds.forEach((name, id) -> {
            generator.json(CreateDieselGenerators.rl("models/item/lighter/"+id), generateLighterSkinModel(id, LighterModel.LighterState.CLOSED));
            generator.json(CreateDieselGenerators.rl("models/item/lighter/"+id+"_open"), generateLighterSkinModel(id, LighterModel.LighterState.OPEN));
            generator.json(CreateDieselGenerators.rl("models/item/lighter/"+id+"_ignited"), generateLighterSkinModel(id, LighterModel.LighterState.IGNITED));
        });
    }

    @Override
    public void generateDataJsons(DataJsonGenerator generator) {
        FuelTypesEventJS event = new FuelTypesEventJS();
        FuelTypesEventJS.addedTypes.clear();
        FUEL_TYPES.post(event);

        AtomicInteger i = new AtomicInteger();
        FuelTypesEventJS.addedTypes.forEach((s, type) -> {
            generator.json(new ResourceLocation("createdieselgenerators", "createdieselgenerators/createdieselgenerators/fuel_type/t" + i), generateFuelType(s, type));
            i.getAndIncrement();
        });
    }

    @Override
    public void generateLang(LangEventJS event) {
        MoldEventJS.addedMolds.forEach((rl, name) -> {
            event.add("mold." + rl.getNamespace() + "." + rl.getPath(), name);
        });
    }

    JsonElement generateTextureModel(ResourceLocation rl) {
        JsonObject object = new JsonObject();
        object.add("parent", new JsonPrimitive("minecraft:item/generated"));
        JsonObject texturesObject = new JsonObject();
        texturesObject.add("layer0", new JsonPrimitive(rl.toString()));
        object.add("textures", texturesObject);
        return object;
    }

    JsonElement generateLighterSkinModel(String id, LighterModel.LighterState state){
        JsonObject object = new JsonObject();
        object.add("parent", new JsonPrimitive("minecraft:item/generated"));
        JsonObject texturesObject = new JsonObject();
        texturesObject.add("layer0", new JsonPrimitive("kubejs:item/lighter/"+id+state.getSuffix()));
        object.add("textures", texturesObject);
        return object;
    }

    JsonElement generateFuelType(String s, FuelType type) {
        JsonObject object = new JsonObject();
        JsonObject normalObject = new JsonObject();
        JsonObject modularObject = new JsonObject();
        JsonObject hugeObject = new JsonObject();
        object.addProperty("fluid", s);

        normalObject.addProperty("speed", type.normal().speed());
        normalObject.addProperty("strength", type.normal().strength());
        normalObject.addProperty("burn_rate", type.normal().burn());

        modularObject.addProperty("speed", type.modular().speed());
        modularObject.addProperty("strength", type.modular().strength());
        modularObject.addProperty("burn_rate", type.modular().burn());

        hugeObject.addProperty("speed", type.huge().speed());
        hugeObject.addProperty("strength", type.huge().strength());
        hugeObject.addProperty("burn_rate", type.huge().burn());

        object.add("normal", normalObject);
        object.add("modular", modularObject);
        object.add("huge", hugeObject);

        object.addProperty("sound_pitch", type.soundPitch());
        object.addProperty("burner_multiplier", type.burnerStrength());
        return object;
    }
}
