package com.jesz.createdieselgenerators.compat;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;

public class EveryCompatCompat {
    public static void init() {
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                new WoodType.Finder(ResourceLocation.fromNamespaceAndPath(CreateDieselGenerators.ID, "chip_wood"))
                .log("chip_wood_beam")
                .planks("chip_wood_block"));
    }
}
