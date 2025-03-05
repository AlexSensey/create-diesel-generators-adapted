package com.jesz.createdieselgenerators.compat.kubejs;

import com.jesz.createdieselgenerators.content.molds.MoldType;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;

public class MoldEventJS extends StartupEventJS {
    @Info("Adds new mold types used for compression molding and casting recipes")
    public void create(String name){
        new MoldType(new ResourceLocation("kubejs", name));
    }
}
