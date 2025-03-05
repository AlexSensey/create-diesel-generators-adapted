package com.jesz.createdieselgenerators.content.entity_filter;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ReverseLootTable implements PreparableReloadListener {
    public static final ReverseLootTable INSTANCE = new ReverseLootTable();

    public static final Map<Item, List<EntityType<?>>> ALL = new HashMap<>();

    public final CompletableFuture<Void> reload(PreparationBarrier p_10780_, ResourceManager p_10781_, ProfilerFiller p_10782_, ProfilerFiller p_10783_, Executor p_10784_, Executor p_10785_) {
        return CompletableFuture.supplyAsync(() -> {
            ALL.clear();
            return null;
        }, p_10784_).thenCompose(p_10780_::wait).thenAcceptAsync((p_10792_) -> {}, p_10785_);
    }
}
