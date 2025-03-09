package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.content.molds.MoldType;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoldEventJS extends StartupEventJS {
    public static Map<ResourceLocation, String> addedMolds = new HashMap<>();
    @Info("Adds new mold types used for compression molding and casting recipes")
    public void create(String name, String langName){
        new MoldType(new ResourceLocation("kubejs", name));
        addedMolds.put(new ResourceLocation("kubejs", name), langName);
    }
}
