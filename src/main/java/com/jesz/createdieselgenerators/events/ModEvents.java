package com.jesz.createdieselgenerators.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.compat.kubejs.CDGKubeJSPlugin;
import com.jesz.createdieselgenerators.compat.kubejs.LighterSkinsEventJS;
import com.jesz.createdieselgenerators.content.canister.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.content.molds.BasinSpoutCasting;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagComponent;
import com.jesz.createdieselgenerators.content.turret.TurretOperatorHatLayer;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.jesz.createdieselgenerators.content.turret.TurretOperatorHatLayer.registerOn;

@Mod.EventBusSubscriber(modid = CreateDieselGenerators.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelRegistry(ModelEvent.RegisterAdditional event){

        for(MoldType type : MoldType.types)
            event.register(type.getModelId());


        LighterModel.lighterSkinIDs.clear();
        Minecraft.getInstance().getResourceManager().getNamespaces().stream().toList().forEach(n -> {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(n, "lighter_skins.json"));
            if(resource.isEmpty())
                return;
            JsonParser parser = new JsonParser();
            try {
                JsonElement data = parser.parse(resource.get().openAsReader());
                data.getAsJsonArray().forEach(jsonElement -> {
                    LighterModel.lighterSkinIDs.put(jsonElement.getAsJsonObject().getAsJsonPrimitive("name").getAsString(), jsonElement.getAsJsonObject().getAsJsonPrimitive("id").getAsString());
                });
            }catch (IOException ignored) {}
        });
        if(ModList.get().isLoaded("kubejs")) {
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
    @OnlyIn(Dist.CLIENT)
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (MoldType type : MoldType.types)
            type.model = models.get(type.getModelId());
    }
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(CDGBlockEntityTypes.CANISTER.get(), new SpoutCanisterFilling());
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(AllBlockEntityTypes.BASIN.get(), new BasinSpoutCasting());
        });
    }

}
