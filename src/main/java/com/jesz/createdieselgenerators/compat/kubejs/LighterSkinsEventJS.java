package com.jesz.createdieselgenerators.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.HashMap;
import java.util.Map;

public class LighterSkinsEventJS implements KubeEvent {
    public static final Map<String, String> addedIds = new HashMap();
    public static final Map<String, String> removedIds = new HashMap();
    @Info("Adds a new lighter skin")
    public void create(String id, String name){
        addedIds.put(name, id);
    }
    @Info("Removes a new lighter skin")
    public void remove(String id, String name){
        removedIds.put(name, id);
    }
}
