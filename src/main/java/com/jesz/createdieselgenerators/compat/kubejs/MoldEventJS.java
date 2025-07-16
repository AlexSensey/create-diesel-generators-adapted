package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.content.molds.MoldType;
import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class MoldEventJS implements KubeStartupEvent {
    public static Map<ResourceLocation, String> addedMolds = new HashMap<>();

    @Info("Adds new mold types used for compression molding and casting recipes")
    public void create(String name, String langName){
        new MoldType(ResourceLocation.fromNamespaceAndPath("kubejs", name));
        addedMolds.put(ResourceLocation.fromNamespaceAndPath("kubejs", name), langName);
    }
}
