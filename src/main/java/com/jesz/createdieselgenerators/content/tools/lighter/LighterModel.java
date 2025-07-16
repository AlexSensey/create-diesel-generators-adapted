package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LighterModel {

    public enum LighterState {
        CLOSED, OPEN, IGNITED;

        public String getSuffix() {
            if(this == CLOSED)
                return "";
            return "_" + CreateLang.asId(name());
        }
    }

    private static final List<LighterModel> ALL = new ArrayList<>();

    protected final ResourceLocation modelLocation;
    protected BakedModel bakedModel;

    public LighterModel(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        ALL.add(this);
    }
    public static LighterModel simple(String id, LighterState state){
        return new LighterModel(CreateDieselGenerators.rl("item/lighter/"+id+state.getSuffix()));
    }
    public static void onModelRegistry(ModelEvent.RegisterAdditional event) {
        for (LighterModel partial : ALL)
            event.register(new ModelResourceLocation(partial.getLocation(), ModelResourceLocation.STANDALONE_VARIANT));
    }

    public static void onModelBake(ModelEvent.BakingCompleted event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (LighterModel partial : ALL)
            partial.set(models.get(new ModelResourceLocation(partial.getLocation(), ModelResourceLocation.STANDALONE_VARIANT)));
    }

    protected void set(BakedModel bakedModel) {
        this.bakedModel = bakedModel;
    }

    public ResourceLocation getLocation() {
        return modelLocation;
    }

    public BakedModel get() {
        return bakedModel;
    }
    public static Map<String, LighterSkinEntry> lighterSkinModels = new HashMap<>();
    public static Map<String, String> lighterSkinIDs = new HashMap<>();

    public static void initSkins(){
        lighterSkinModels.clear();
        lighterSkinModels.put("standard", LighterSkinEntry.STANDARD);
        lighterSkinIDs.forEach((name, id) -> {
            lighterSkinModels.put(id, LighterSkinEntry.simple(name, id));
        });

    }

}
