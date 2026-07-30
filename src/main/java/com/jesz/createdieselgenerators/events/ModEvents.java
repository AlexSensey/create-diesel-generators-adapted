package com.jesz.createdieselgenerators.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.compat.kubejs.LighterSkinsEventJS;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterUnpackingHandler;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.jesz.createdieselgenerators.content.canister.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.molds.BasinSpoutCasting;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagComponent;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.jesz.createdieselgenerators.content.turret.TurretOperatorHatLayer;
import com.jesz.createdieselgenerators.events.datagen.CDGRecipeProvider;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.jesz.createdieselgenerators.ponder.CDGPonderPlugin;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CreateDieselGenerators.ID)
public class ModEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void gatherData(GatherDataEvent event) {
        
        CreateDieselGenerators.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {

            JsonElement jsonElement = FilesHelper.loadJsonResource("assets/createdieselgenerators/lang/default.json");
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                provider.add(entry.getKey(), entry.getValue().getAsString());


            PonderIndex.addPlugin(new CDGPonderPlugin());
            PonderIndex.getLangAccess().provideLang(CreateDieselGenerators.ID, provider::add);
        });

        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = event.getGenerator().getPackOutput();

        event.addProvider(new CDGRecipeProvider(packOutput, lookupProvider));
    }

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                CDGRegistries.FUEL_TYPE,
                FuelType.CODEC,
                FuelType.NCODEC
        );
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(ModelEvent.RegisterAdditional event){

        event.register(ModelResourceLocation.standalone(CreateDieselGenerators.rl("block/girder_strut/andesite_girder")));

        for (MoldType type : MoldType.types)
            event.register(new ModelResourceLocation(type.getModelId(), ModelResourceLocation.STANDALONE_VARIANT));


        LighterModel.lighterSkinIDs.clear();
        Minecraft.getInstance().getResourceManager().getNamespaces().stream().toList().forEach(n -> {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath(n, "lighter_skins.json"));
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
        LighterModel.onModelRegistry(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerClientTooltips(RegisterClientTooltipComponentFactoriesEvent event){
        event.register(TrackLayersBagComponent.class,
                c -> c);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void addEntityRendererLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderDispatcher dispatcher = Minecraft.getInstance()
                .getEntityRenderDispatcher();

        TurretOperatorHatLayer.registerOnAll(dispatcher);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, c) -> ((FueledToolItem)stack.getItem()).getFluidHandler(stack),
                CDGItems.LIGHTER,
                CDGItems.CHEMICAL_SPRAYER,
                CDGItems.CHEMICAL_SPRAYER_LIGHTER,
                CDGBlocks.CANISTER);

        for (FluidEntry<BaseFlowingFluid.Flowing> e : CDGFluids.CONCRETE) {
            event.registerItem(
                    Capabilities.FluidHandler.ITEM,
                    (stack, c) -> new FluidBucketWrapper(stack),
                    e.getBucket().orElseThrow()
            );
        }
        BulkFermenterBlockEntity.registerCapabilities(event);
        BurnerBlockEntity.registerCapabilities(event);
        CanisterBlockEntity.registerCapabilities(event);
        DieselEngineBlockEntity.registerCapabilities(event);
        ModularDieselEngineBlockEntity.registerCapabilities(event);
        HugeDieselEngineBlockEntity.registerCapabilities(event);
        DistillationTankBlockEntity.registerCapabilities(event);
        OilBarrelBlockEntity.registerCapabilities(event);
        PumpjackHoleBlockEntity.registerCapabilities(event);
        ChemicalTurretBlockEntity.registerCapabilities(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        CDGItems.LIGHTER.get().registerExtension(event);
        CDGItems.CHEMICAL_SPRAYER.get().registerExtension(event);
        CDGItems.CHEMICAL_SPRAYER_LIGHTER.get().registerExtension(event);
        CDGItems.HAMMER.get().registerExtension(event);
        CDGItems.WIRE_CUTTERS.get().registerExtension(event);
        CDGItems.MOLD.get().registerExtension(event);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (MoldType type : MoldType.types)
            type.model = models.get(new ModelResourceLocation(type.getModelId(), ModelResourceLocation.STANDALONE_VARIANT));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CDGFluids.PLANT_OIL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CDGFluids.PLANT_OIL.getSource(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CDGFluids.ETHANOL.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(CDGFluids.ETHANOL.getSource(), RenderType.translucent());
        PonderIndex.addPlugin(new CDGPonderPlugin());
        event.enqueueWork(CDGSpriteShifts::init);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(CDGBlockEntityTypes.CANISTER.get(), new SpoutCanisterFilling());
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(AllBlockEntityTypes.BASIN.get(), new BasinSpoutCasting());
            BulkFermenterUnpackingHandler.register();
        });
    }

}
