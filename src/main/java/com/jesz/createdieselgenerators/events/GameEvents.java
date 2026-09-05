package com.jesz.createdieselgenerators.events;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.mixins.LootItemAccessor;
import com.jesz.createdieselgenerators.mixins.LootPoolAccessor;
import com.jesz.createdieselgenerators.mixins.LootTableAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = CreateDieselGenerators.ID)
public final class GameEvents {
    private static final Map<Level, Set<BlockPos>> TO_EXPLODE = new HashMap<>();

    private GameEvents() {}

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        for (CDGRecipes recipe : CDGRecipes.values())
            event.sendRecipes(recipe.getType());
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        new CDGCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public static void loadLootTable(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        Identifier tableId = table.getLootTableId();
        if (tableId == null || !tableId.getPath().startsWith("entities/"))
            return;

        ((LootTableAccessor) table).getPools().forEach(pool ->
                List.of(((LootPoolAccessor) pool).getEntries()).forEach(entries -> {
                    for (LootPoolEntryContainer entry : entries) {
                        if (!(entry instanceof LootItemAccessor lootItem))
                            continue;
                        String path = tableId.getPath().replace("entities/", "");
                        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE
                                .get(Identifier.fromNamespaceAndPath(tableId.getNamespace(), path))
                                .map(holder -> holder.value())
                                .orElse(null);
                        if (type != null)
                            ReverseLootTable.ALL.computeIfAbsent(lootItem.getItem().value(), ignored -> new ArrayList<>())
                                    .add(type);
                    }
                }));
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    }

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level))
            return;
        Set<BlockPos> queued = TO_EXPLODE.get(level);
        if (queued == null || queued.isEmpty())
            return;
        for (BlockPos pos : List.copyOf(queued)) {
            level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(),
                    1, true, Level.ExplosionInteraction.BLOCK);
            queued.remove(pos);
        }
        if (queued.isEmpty())
            TO_EXPLODE.remove(level);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (!CDGConfig.COMBUSTIBLES_BLOW_UP.get() || level.isClientSide())
            return;

        for (int x = -2; x < 2; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -2; z < 2; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) >= 2)
                        continue;
                    BlockPos pos = new BlockPos(
                            (int) (x + event.getExplosion().center().x),
                            (int) (y + event.getExplosion().center().y),
                            (int) (z + event.getExplosion().center().z));
                    if (!level.isInWorldBounds(pos))
                        continue;
                    FluidState fluidState = level.getFluidState(pos);
                    boolean flammable = FuelType.getTypeFor(
                            level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluidState.getType())
                            .normal().speed() != 0;
                    if (!flammable)
                        continue;
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    TO_EXPLODE.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos);
                    return;
                }
            }
        }
    }
}
