package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.CreateDieselGenerators;

public record LighterSkinEntry(String name, LighterModel closedModel, LighterModel openModel, LighterModel ignitedModel) {
    public static final LighterSkinEntry STANDARD = new LighterSkinEntry(
            "standard",
            new LighterModel(CreateDieselGenerators.rl("item/lighter")),
            new LighterModel(CreateDieselGenerators.rl("item/lighter_open")),
            new LighterModel(CreateDieselGenerators.rl("item/lighter_ignited"))
    );
    public static LighterSkinEntry simple(String name, String id){
        return new LighterSkinEntry(
                name, LighterModel.simple(id, LighterModel.LighterState.CLOSED)
                , LighterModel.simple(id, LighterModel.LighterState.OPEN)
                , LighterModel.simple(id, LighterModel.LighterState.IGNITED)
        );
    }
}
