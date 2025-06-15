package com.jesz.createdieselgenerators.fuel_type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import net.createmod.catnip.data.Couple;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FuelType {
    float normalSpeed;
    float modularSpeed;
    float hugeSpeed;

    float normalStrength;
    float modularStrength;
    float hugeStrength;

    float burnerStrength;

    int normalBurn;
    int modularBurn;
    int hugeBurn;
    int soundSpeed;
    public FuelType(float normalSpeed, float normalStrength, int normalBurn,
                    float modularSpeed, float modularStrength, int modularBurn,
                    float hugeSpeed, float hugeStrength, int hugeBurn, int soundSpeed, float burnerStrength){
        this.normalSpeed = normalSpeed;
        this.modularSpeed = modularSpeed;
        this.hugeSpeed = hugeSpeed;
        this.normalStrength = normalStrength;
        this.modularStrength = modularStrength;
        this.hugeStrength = hugeStrength;
        this.normalBurn = normalBurn;
        this.modularBurn = modularBurn;
        this.hugeBurn = hugeBurn;
        this.soundSpeed = soundSpeed;
        this.burnerStrength = burnerStrength;
    }

    public static FuelType fromJSON(JsonElement element) {
        if (!element.getAsJsonObject().has("normal"))
            throw new JsonSyntaxException("Invalid Fuel Type: missing fuel values for normal engine");

        JsonObject normalEngineObject = element.getAsJsonObject().get("normal").getAsJsonObject();
        JsonObject modularEngineObject = element.getAsJsonObject().has("modular") ? element.getAsJsonObject().get("modular").getAsJsonObject() : normalEngineObject;
        JsonObject hugeEngineObject = element.getAsJsonObject().has("huge") ? element.getAsJsonObject().get("huge").getAsJsonObject() : normalEngineObject;

        return new FuelType(
                normalEngineObject.has("speed") ? normalEngineObject.get("speed").getAsFloat(): 16,
                normalEngineObject.has("strength") ? normalEngineObject.get("strength").getAsFloat() : 1024,
                normalEngineObject.has("burn_rate") ? normalEngineObject.get("burn_rate").getAsInt() : 1,
                modularEngineObject.has("speed") ? modularEngineObject.get("speed").getAsFloat(): 16,
                modularEngineObject.has("strength") ? modularEngineObject.get("strength").getAsFloat() : 1024,
                modularEngineObject.has("burn_rate") ? modularEngineObject.get("burn_rate").getAsInt() : 1,
                hugeEngineObject.has("speed") ? hugeEngineObject.get("speed").getAsFloat(): 16,
                hugeEngineObject.has("strength") ? hugeEngineObject.get("strength").getAsFloat() : 1024,
                hugeEngineObject.has("burn_rate") ? hugeEngineObject.get("burn_rate").getAsInt() : 1,
                element.getAsJsonObject().has("sound_speed") ? element.getAsJsonObject().get("sound_speed").getAsInt() : 1,
                element.getAsJsonObject().has("burner_multiplier") ? element.getAsJsonObject().get("burner_multiplier").getAsFloat() : 1
        );
    }

    public float getBurnerStrength() {
        return burnerStrength;
    }

    public Couple<Float> getGenerated(BlockEntity be) {
        if(be instanceof HugeDieselEngineBlockEntity)
            return getGeneratedHuge();
        if(be instanceof ModularDieselEngineBlockEntity)
            return getGeneratedModular();
        return getGeneratedNormal();
    }

    public Couple<Float> getGeneratedNormal() {return Couple.create(normalSpeed, normalStrength);}
    public Couple<Float> getGeneratedModular() {return Couple.create(modularSpeed, modularStrength);}
    public Couple<Float> getGeneratedHuge() {return Couple.create(hugeSpeed, hugeStrength);}

    public int getBurn(BlockEntity be) {
        if(be instanceof HugeDieselEngineBlockEntity)
            return getBurnHuge();
        if(be instanceof ModularDieselEngineBlockEntity)
            return getBurnModular();
        return getBurnNormal();
    }

    public int getBurnNormal(){ return normalBurn; }
    public int getBurnModular(){ return modularBurn; }
    public int getBurnHuge(){ return hugeBurn; }
    public int getSoundSpeed() { return soundSpeed; }
}
