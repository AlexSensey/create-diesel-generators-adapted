package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.fuel_type.FuelType;
import net.minecraft.core.HolderSet;

import java.util.List;
import java.util.function.Consumer;

public class FuelTypeBuilder {
    float normalSpeed;
    float modularSpeed;
    float hugeSpeed;

    float normalStrength;
    float modularStrength;
    float hugeStrength;

    float burnerStrength;

    float normalBurn;
    float modularBurn;
    float hugeBurn;
    float pitch;
    Consumer<FuelType> callback;
    public FuelTypeBuilder(Consumer<FuelType> callback) {
        this.callback = callback;
    }

    public FuelTypeBuilder normalSpeed(float speed) {
        normalSpeed = speed;
        return this;
    }

    public FuelTypeBuilder modularSpeed(float speed) {
        modularSpeed = speed;
        return this;
    }

    public FuelTypeBuilder hugeSpeed(float speed) {
        hugeSpeed = speed;
        return this;
    }

    public FuelTypeBuilder normalStrength(float strength) {
        normalStrength = strength;
        return this;
    }

    public FuelTypeBuilder modularStrength(float strength) {
        modularStrength = strength;
        return this;
    }

    public FuelTypeBuilder hugeStrength(float strength) {
        hugeStrength = strength;
        return this;
    }

    public FuelTypeBuilder normalBurn(float burn) {
        normalBurn = burn;
        return this;
    }

    public FuelTypeBuilder modularBurn(float burn) {
        modularBurn = burn;
        return this;
    }

    public FuelTypeBuilder hugeBurn(float burn) {
        hugeBurn = burn;
        return this;
    }

    public FuelTypeBuilder soundPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public FuelTypeBuilder burnerStrength(float burnerStrength) {
        this.burnerStrength = burnerStrength;
        return this;
    }

    public FuelType build() {

        FuelType type = new FuelType(HolderSet.direct(List.of()), new FuelType.PerEngineProperties(normalSpeed, normalStrength, normalBurn)
                , new FuelType.PerEngineProperties(modularSpeed, modularStrength, modularBurn)
                , new FuelType.PerEngineProperties(hugeSpeed, hugeStrength, hugeBurn), pitch, burnerStrength);
        callback.accept(type);
        return type;
    }
}
