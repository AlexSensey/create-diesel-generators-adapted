package com.jesz.createdieselgenerators.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.level.ChunkPos;

public class GetChunkOilAmountEventJS extends EventJS {
    public ChunkPos chunkPos;
    public String[] biomes;
    public long seed;
}
