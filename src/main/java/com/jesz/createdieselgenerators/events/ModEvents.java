package com.jesz.createdieselgenerators.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.compat.kubejs.LighterSkinsEventJS;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterRenderer;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterUnpackingHandler;
import com.jesz.createdieselgenerators.content.basin_lid.BasinLidRenderer;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.burner.BurnerRenderer;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterRenderer;
import com.jesz.createdieselgenerators.content.canister.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineRenderer;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineRenderer;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineRenderer;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankRenderer;
import com.jesz.createdieselgenerators.content.molds.BasinSpoutCasting;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.NoShaftBearingRenderer;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankRenderer;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleRenderer;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerProjectileRenderer;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagComponent;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretRenderer;
import com.jesz.createdieselgenerators.content.turret.TurretOperatorHatLayer;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.ponder.CDGPonderPlugin;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.FluidEntry;
import dev.engine_room.flywheel.lib.model.baked.PartialModelEventHandler;
import net.createmod.ponder.api.client.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateDieselGenerators.ID)
public class ModEvents {

    @SubscribeEvent
    public static void onModelRegistry(ModelEvent.RegisterStandalone event){

        CDGPartialModels.init();
        PartialModelEventHandler.onRegisterStandalone(event);

        LighterModel.lighterSkinIDs.clear();
        Minecraft.getInstance().getResourceManager().getNamespaces().stream().toList().forEach(n -> {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath(n, "lighter_skins.json"));
            if (resource.isEmpty())
                return;
            try {
                JsonElement data = JsonParser.parseReader(resource.get().openAsReader());
                data.getAsJsonArray().forEach(jsonElement ->
                        LighterModel.lighterSkinIDs.put(jsonElement.getAsJsonObject().getAsJsonPrimitive("name").getAsString(), jsonElement.getAsJsonObject().getAsJsonPrimitive("id").getAsString()));
            } catch (IOException ignored) {}
        });

        if (ModList.get().isLoaded("kubejs")) {
            LighterModel.lighterSkinIDs.putAll(LighterSkinsEventJS.addedIds);
            LighterSkinsEventJS.removedIds.forEach((name, id) -> LighterModel.lighterSkinIDs.remove(name, id));
        }

        LighterModel.initSkins();
    }

    @SubscribeEvent
    public static void registerClientTooltips(RegisterClientTooltipComponentFactoriesEvent event){
        event.register(TrackLayersBagComponent.class,
                c -> c);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CDGEntityTypes.CHEMICAL_SPRAYER_PROJECTILE.get(), ChemicalSprayerProjectileRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.BURNER.get(), BurnerRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.CHEMICAL_TURRET.get(), ChemicalTurretRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.DIESEL_ENGINE.get(), DieselEngineRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(), ModularDieselEngineRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.HUGE_DIESEL_ENGINE.get(), HugeDieselEngineRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.POWERED_ENGINE_SHAFT.get(), KineticBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.BASIN_LID.get(), BasinLidRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.PUMPJACK_BEARING.get(), NoShaftBearingRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.CANISTER.get(), CanisterRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.DISTILLATION_TANK.get(), DistillationTankRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.BULK_FERMENTER.get(), BulkFermenterRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.PUMPJACK_HOLE.get(), PumpjackHoleRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.PUMPJACK_CRANK.get(), PumpjackCrankRenderer::new);
        event.registerBlockEntityRenderer(CDGBlockEntityTypes.ENCASED_GIRDER.get(), ShaftRenderer::new);
    }

    @SubscribeEvent
    public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance()
                .getEntityRenderDispatcher();

        TurretOperatorHatLayer.registerOnAll(dispatcher);
    }

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CDGPonderPlugin());
        event.enqueueWork(CDGSpriteShifts::init);
    }

}
