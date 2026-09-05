package com.jesz.createdieselgenerators.events;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderWrenchBehaviourClient;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringRenderer;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagPlacementClient;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CKinetics;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.api.lang.Lang;
import net.createmod.catnip.api.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateDieselGenerators.ID)
public final class ClientGameEvents {
    private ClientGameEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        AndesiteGirderWrenchBehaviourClient.tick();
        EntityFilteringRenderer.tick();
        TrackLayersBagPlacementClient.clientTick();
    }

    @SubscribeEvent
    public static void submitCustomGeometry(SubmitCustomGeometryEvent event) {
        TrackLayersBagPlacementClient.submit(event.getPoseStack(), event.getSubmitNodeCollector(),
                event.getLevelRenderState().cameraRenderState);
    }

    @SubscribeEvent
    public static void addToItemTooltip(ItemTooltipEvent event) {
        if (!AllConfigs.client().tooltips.get() || event.getEntity() == null || Minecraft.getInstance().level == null)
            return;

        List<Component> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        if ((item instanceof BucketItem || item == Items.MILK_BUCKET) && CDGConfig.FUEL_TOOLTIPS.get()) {
            Fluid fluid = item instanceof BucketItem bucket ? bucket.content : NeoForgeMod.MILK.get();
            FuelType type = FuelType.getTypeFor(
                    Minecraft.getInstance().level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluid);
            addFuelTooltip(tooltip, type);
        }

        if (!(item instanceof BlockItem blockItem)
                || !IRotate.StressImpact.isEnabled()
                || !(CDGBlocks.DIESEL_ENGINE.is(blockItem)
                || CDGBlocks.MODULAR_DIESEL_ENGINE.is(blockItem)
                || CDGBlocks.HUGE_DIESEL_ENGINE.is(blockItem)))
            return;

        addEngineTooltip(event, tooltip, blockItem);
    }

    private static void addFuelTooltip(List<Component> tooltip, FuelType type) {
        if (!com.simibubi.create.AllKeys.altDown() || type.normal().speed() == 0) {
            if (type.normal().speed() != 0)
                tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats",
                        Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(1, Component.translatable("createdieselgenerators.tooltip.holdForFuelStats",
                Component.translatable("createdieselgenerators.tooltip.keyAlt").withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(2, Component.empty());
        List<EngineTypes> enabledEngines = Arrays.stream(EngineTypes.values()).filter(EngineTypes::enabled).toList();
        if (enabledEngines.isEmpty())
            return;

        int currentEngineIndex = (AnimationTickHolder.getTicks() % 120) / 20;
        EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enabledEngines.size());
        boolean multipleEngines = enabledEngines.size() != 1;
        if (multipleEngines)
            tooltip.add(3, Component.translatable("block.createdieselgenerators."
                    + (currentEngine == EngineTypes.MODULAR ? "large_"
                    : currentEngine == EngineTypes.HUGE ? "huge_" : "") + "diesel_engine")
                    .withStyle(ChatFormatting.GRAY));

        int index = multipleEngines ? 4 : 3;
        addFuelStat(tooltip, index++, "fuelSpeed", type.getGenerated(currentEngine).speed());
        addFuelStat(tooltip, index++, "fuelStress", type.getGenerated(currentEngine).strength());
        addFuelStat(tooltip, index++, "fuelBurnRate", type.getGenerated(currentEngine).burn() * 20);
        tooltip.add(index++, Component.empty());
        tooltip.add(index++, Component.translatable("createdieselgenerators.tooltip.burnerStrength",
                CreateLang.number(type.burnerStrength() * 100).text(" %").component()
                        .withStyle(FontHelper.Palette.STANDARD_CREATE.primary()))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(index, Component.empty());
    }

    private static void addFuelStat(List<Component> tooltip, int index, String key, float value) {
        tooltip.add(index, Component.translatable("createdieselgenerators.tooltip." + key,
                CreateLang.number(value).component().withStyle(FontHelper.Palette.STANDARD_CREATE.primary()))
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void addEngineTooltip(ItemTooltipEvent event, List<Component> tooltip, BlockItem blockItem) {
        CKinetics config = AllConfigs.server().kinetics;
        int highestCapacity = 0;
        int highestStressCapacity = 0;
        for (var holder : Minecraft.getInstance().level.registryAccess()
                .lookupOrThrow(CDGRegistries.FUEL_TYPE).listElements().toList()) {
            FuelType type = holder.value();
            float speed;
            float strength;
            if (CDGBlocks.DIESEL_ENGINE.is(blockItem)) {
                speed = type.normal().speed();
                strength = type.normal().strength();
            } else if (CDGBlocks.MODULAR_DIESEL_ENGINE.is(blockItem)) {
                speed = type.modular().speed();
                strength = type.modular().strength();
            } else {
                speed = type.huge().speed();
                strength = type.huge().strength();
            }
            if (speed != 0)
                highestCapacity = (int) Math.max(highestCapacity, strength / speed);
            highestStressCapacity = (int) Math.max(highestStressCapacity, strength);
        }

        tooltip.add(Component.empty());
        tooltip.add(CreateLang.translate("tooltip.capacityProvided").style(GRAY).component());
        IRotate.StressImpact impactId = highestCapacity >= config.highCapacity.get()
                ? IRotate.StressImpact.HIGH
                : highestCapacity >= config.mediumCapacity.get()
                ? IRotate.StressImpact.MEDIUM : IRotate.StressImpact.LOW;
        IRotate.StressImpact opposite = IRotate.StressImpact.values()[
                IRotate.StressImpact.values().length - 2 - impactId.ordinal()];
        LangBuilder builder = CreateLang.builder()
                .add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
                        .style(opposite.getAbsoluteColor()));

        if (GogglesItem.isWearingGoggles(event.getEntity())) {
            LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");
            LangBuilder stressUnit = CreateLang.translate("generic.unit.stress");
            tooltip.add(builder.add(CreateLang.number(highestCapacity)).text("x ").add(rpmUnit).component());
            LangBuilder amount = CreateLang.number(highestStressCapacity).add(stressUnit);
            tooltip.add(CreateLang.text(" -> ").add(CreateLang.translate("tooltip.up_to", amount))
                    .style(DARK_GRAY).component());
        } else {
            tooltip.add(builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name())).component());
        }
    }
}
