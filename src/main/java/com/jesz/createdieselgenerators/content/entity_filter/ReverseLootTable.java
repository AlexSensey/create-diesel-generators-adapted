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

    public final CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller workerProfiler, ProfilerFiller mainProfiler, Executor workerExecutor, Executor mainExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            ALL.clear();
            return null;
        }, workerExecutor).thenCompose(stage::wait).thenAcceptAsync((o) -> {}, mainExecutor);
    }
}
