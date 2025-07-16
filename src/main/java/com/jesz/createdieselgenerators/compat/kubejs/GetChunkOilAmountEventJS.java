package com.jesz.createdieselgenerators.compat.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.level.ChunkPos;

public class GetChunkOilAmountEventJS implements KubeEvent {
    public ChunkPos chunkPos;
    public String[] biomes;
    public long seed;
}
