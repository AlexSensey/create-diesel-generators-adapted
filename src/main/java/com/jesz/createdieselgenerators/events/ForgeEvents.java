package com.jesz.createdieselgenerators.events;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.commands.CDGCommands;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderWrenchBehaviour;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringRenderer;
import com.jesz.createdieselgenerators.content.entity_filter.ReverseLootTable;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.jesz.createdieselgenerators.mixins.LootPoolAccessor;
import com.jesz.createdieselgenerators.mixins.LootTableAccessor;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.jesz.createdieselgenerators.packets.FuelTypesUpdatePacket;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.*;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

@Mod.EventBusSubscriber(modid = CreateDieselGenerators.ID)
public class ForgeEvents {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        new CDGCommands(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
    @SubscribeEvent
    public static void loadLootTable(LootTableLoadEvent event){
        LootTable table = event.getTable();
        ResourceLocation tableId = table.getLootTableId();
        if(!tableId.getPath().startsWith("entities/"))
                return;
        List<ItemStack> results = new LinkedList<>();
        ((LootTableAccessor)table).getPools().forEach(pool -> {
            List.of(((LootPoolAccessor) pool).getEntries()).forEach(e -> {
                if(e instanceof LootItem lootItem){
                    lootItem.createItemStack(stack -> {
                        String path = tableId.getPath();
                        path = path.replaceAll("entities/", "");
                        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(tableId.getNamespace(), path));
                        ReverseLootTable.ALL.computeIfAbsent(stack.getItem(), s -> new ArrayList<>()).add(type);

                    },null);
                }
            });
        });
    }

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if(player instanceof ServerPlayer sp)
            CDGPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> sp), new FuelTypesUpdatePacket(FuelTypeManager.fuelTypes));

    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event){
        event.addListener(ReverseLootTable.INSTANCE);
        event.addListener(FuelTypeManager.ReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START)
            return;
        AndesiteGirderWrenchBehaviour.tick();
        EntityFilteringRenderer.tick();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.START || !(event.level instanceof ServerLevel level))
            return;
        if (toExplode.containsKey(level)) {
            List<BlockPos> list = toExplode.get(level).stream().toList();
            if (list.isEmpty())
                return;
            for (BlockPos pos : list) {
                level.explode(null, null, null, pos.getX(), pos.getY(), pos.getZ(), 1, true, Level.ExplosionInteraction.BLOCK);
                toExplode.get(level).remove(pos);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent event){
        if (event.entity instanceof ItemEntity itemEntity)
            if (itemEntity.getItem().is(CDGItems.LIGHTER.get()) && CDGConfig.COMBUSTIBLES_BLOW_UP.get() && itemEntity.getItem().getTag() != null)
                if (itemEntity.getItem().getTag().getInt("Type") == 2) {
                    Vec3 entityPos = itemEntity.getPosition(1);
                    FluidState fState = itemEntity.level().getFluidState(new BlockPos(BlockPos.containing(entityPos)));
                    if(fState.is(Fluids.WATER) || fState.is(Fluids.FLOWING_WATER)) {
                        itemEntity.getItem().getTag().putInt("Type", 1);
                        itemEntity.level().playLocalSound(itemEntity.getPosition(1).x, itemEntity.getPosition(1).y, itemEntity.getPosition(1).z, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f, false);
                        return;
                    }
                    if (FuelTypeManager.getGeneratedSpeed(fState.getType()) != 0)
                        itemEntity.level().explode(null, null, null, itemEntity.getPosition(1).x, itemEntity.getPosition(1).y, itemEntity.getPosition(1).z, 1, true, Level.ExplosionInteraction.BLOCK);
                }
    }

    static Map<Level, Set<BlockPos>> toExplode = new HashMap<>();

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent event){
        Level level = event.getLevel();
        if (CDGConfig.COMBUSTIBLES_BLOW_UP.get() && !level.isClientSide)
            for (int x = -2; x < 2; x++) {
                for (int y = -2; y < 2; y++) {
                    for (int z = -2; z < 2; z++) {
                        BlockPos pos = new BlockPos((int) (x+event.getExplosion().getPosition().x), (int) (y+event.getExplosion().getPosition().y), (int) (z+event.getExplosion().getPosition().z));

                        if (!level.isInWorldBounds(pos)) continue;
                        if (Math.abs(Math.sqrt(x*x+y*y+z*z)) < 2) {
                            FluidState fluidState = level.getFluidState(pos);

                            if (FuelTypeManager.getGeneratedSpeed(fluidState.getType()) != 0) {
                                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                if (!toExplode.containsKey(level))
                                    toExplode.put(level, new HashSet<>());
                                toExplode.get(level).add(pos);
                                return;
                            }
                        }
                    }
                }
            }
    }

    @SubscribeEvent
    public static void addTrade(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
        if(!(event.getType() == VillagerProfession.TOOLSMITH))
            return;
        trades.get(2).add((t, r) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 5),
                new ItemStack(CDGItems.LIGHTER.get()),
                10,8,0.02f));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void addToItemTooltip(ItemTooltipEvent event) {
        if (!AllConfigs.client().tooltips.get())
            return;
        if (event.getEntity() == null)
            return;

        List<Component> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        if ((item instanceof BucketItem || item instanceof MilkBucketItem) && CDGConfig.FUEL_TOOLTIPS.get()) {
            Fluid fluid = ForgeMod.MILK.get();
            if(item instanceof BucketItem bi)
                fluid = bi.getFluid();

            if (FuelTypeManager.getGeneratedSpeed(fluid) != 0) {
                if (Screen.hasAltDown()) {
                    tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats", Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(2, Component.empty());

                    byte enginesEnabled = (byte) ((EngineTypes.NORMAL.enabled() ? 1 : 0) + (EngineTypes.MODULAR.enabled() ? 1 : 0) + (EngineTypes.HUGE.enabled() ? 1 : 0));
                    int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
                    List<EngineTypes> enabledEngines = Arrays.stream(EngineTypes.values()).filter(EngineTypes::enabled).toList();
                    EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
                    float currentSpeed = FuelTypeManager.getGeneratedSpeed(currentEngine, fluid);
                    float currentCapacity = FuelTypeManager.getGeneratedStress(currentEngine, fluid);
                    float currentBurn = FuelTypeManager.getBurnRate(currentEngine, fluid);

                    if(enginesEnabled != 1)
                        tooltip.add(3, Component.translatable("block.createdieselgenerators."+
                                (currentEngine == EngineTypes.MODULAR ? "large_" : currentEngine == EngineTypes.HUGE ? "huge_" : "")+"diesel_engine").withStyle(ChatFormatting.GRAY));
                    tooltip.add(enginesEnabled != 1 ? 4 : 3, Component.translatable("createdieselgenerators.tooltip.fuelSpeed", CreateLang.number(currentSpeed).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 5 : 4, Component.translatable("createdieselgenerators.tooltip.fuelStress", CreateLang.number(currentCapacity).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 6 : 5, Component.translatable("createdieselgenerators.tooltip.fuelBurnRate", CreateLang.number(currentBurn).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 7 : 6, Component.empty());
                    tooltip.add(enginesEnabled != 1 ? 8 : 7, Component.translatable("createdieselgenerators.tooltip.burnerStrength", CreateLang.number(FuelTypeManager.getBurnerStrength(fluid) * 100).text(" %").component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary())).withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(enginesEnabled != 1 ? 9 : 8, Component.empty());
                } else {
                    tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats", Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }

        CKinetics config = AllConfigs.server().kinetics;

        if (!(item instanceof BlockItem bi) ||
                !IRotate.StressImpact.isEnabled() ||
                !(CDGBlocks.DIESEL_ENGINE.is(bi) ||
                CDGBlocks.MODULAR_DIESEL_ENGINE.is(bi) ||
                CDGBlocks.HUGE_DIESEL_ENGINE.is(bi)))
            return;

        tooltip.add(Component.empty());

        int highestRPM = 0;
        int highestCapacity = 0;
        int highestStressCapacity = 0;

        for (FuelType type : FuelTypeManager.fuelTypes.values()) {
            if (CDGBlocks.DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.normalSpeed());
                highestCapacity = (int) Math.max(highestCapacity, type.normalStrength() / type.normalSpeed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.normalStrength());
            }
            else if (CDGBlocks.MODULAR_DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.modularSpeed());
                highestCapacity = (int) Math.max(highestCapacity, type.modularStrength() / type.modularSpeed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.modularStrength());
            }
            else if (CDGBlocks.HUGE_DIESEL_ENGINE.is(bi)) {
                highestRPM = (int) Math.max(highestRPM, type.hugeSpeed());
                highestCapacity = (int) Math.max(highestCapacity, type.hugeStrength() / type.hugeSpeed());
                highestStressCapacity = (int) Math.max(highestStressCapacity, type.hugeStrength());
            }
        }
        boolean hasGoggles = GogglesItem.isWearingGoggles(event.getEntity());

        LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");
        LangBuilder suUnit = CreateLang.translate("generic.unit.stress");

        CreateLang.translate("tooltip.capacityProvided")
                .style(GRAY)
                .addTo(tooltip);

        IRotate.StressImpact impactId = highestCapacity >= config.highCapacity.get() ? IRotate.StressImpact.HIGH
                : (highestCapacity >= config.mediumCapacity.get() ? IRotate.StressImpact.MEDIUM : IRotate.StressImpact.LOW);
        IRotate.StressImpact opposite = IRotate.StressImpact.values()[IRotate.StressImpact.values().length - 2 - impactId.ordinal()];
        LangBuilder builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
                        .style(opposite.getAbsoluteColor()));

        if (hasGoggles) {
            builder.add(CreateLang.number(highestCapacity))
                    .text("x ")
                    .add(rpmUnit)
                    .addTo(tooltip);
            LangBuilder amount = CreateLang.number(highestStressCapacity)
                    .add(suUnit);
            CreateLang.text(" -> ")
                    .add(CreateLang.translate("tooltip.up_to", amount))
                    .style(DARK_GRAY)
                    .addTo(tooltip);

        } else
            builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name()))
                    .addTo(tooltip);

    }
}
