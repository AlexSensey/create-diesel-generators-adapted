package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.Identifier;

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

    protected final Identifier modelLocation;
    protected final PartialModel model;

    public LighterModel(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        this.model = PartialModel.of(modelLocation);
        ALL.add(this);
    }
    public static LighterModel simple(String id, LighterState state){
        return new LighterModel(CreateDieselGenerators.id("item/lighter/"+id+state.getSuffix()));
    }

    public Identifier getLocation() {
        return modelLocation;
    }

    public PartialModel get() {
        return model;
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
