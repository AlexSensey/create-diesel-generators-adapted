package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.fuel_type.FuelType;

import java.util.function.Consumer;

public class FuelTypeBuilder {
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
    Consumer<FuelType> callback;
    public FuelTypeBuilder(Consumer<FuelType> callback) {
        this.callback = callback;
    }

    public FuelTypeBuilder normalSpeed(float speed){
        normalSpeed = speed;
        return this;
    }
    public FuelTypeBuilder modularSpeed(float speed){
        modularSpeed = speed;
        return this;
    }
    public FuelTypeBuilder hugeSpeed(float speed){
        hugeSpeed = speed;
        return this;
    }

    public FuelTypeBuilder normalStrength(float strength){
        normalStrength = strength;
        return this;
    }
    public FuelTypeBuilder modularStrength(float strength){
        modularStrength = strength;
        return this;
    }
    public FuelTypeBuilder hugeStrength(float strength){
        hugeStrength = strength;
        return this;
    }

    public FuelTypeBuilder normalBurn(int burn){
        normalBurn = burn;
        return this;
    }
    public FuelTypeBuilder modularBurn(int burn){
        modularBurn = burn;
        return this;
    }
    public FuelTypeBuilder hugeBurn(int burn){
        hugeBurn = burn;
        return this;
    }

    public FuelTypeBuilder soundSpeed(int soundSpeed){
        this.soundSpeed = soundSpeed;
        return this;
    }

    public FuelTypeBuilder burnerStrength(float burnerStrength){
        this.burnerStrength = burnerStrength;
        return this;
    }

    public FuelType build(){
        FuelType type = new FuelType(normalSpeed, normalStrength, normalBurn,
                modularSpeed, modularStrength, modularBurn,
                hugeSpeed, hugeStrength, hugeBurn, soundSpeed, burnerStrength);
        callback.accept(type);
        return type;
    }
}
