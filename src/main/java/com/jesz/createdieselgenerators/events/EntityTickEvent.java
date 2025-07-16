package com.jesz.createdieselgenerators.events;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class EntityTickEvent extends Event {
    public EntityTickEvent(Object entity){
        this.entity = (Entity)entity;
    }
    public Entity entity;
}
