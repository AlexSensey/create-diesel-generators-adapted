package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.compat.EveryCompatCompat;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.compat.strut_your_stuff.StrutYourStuffRegistryEntries;
import com.jesz.createdieselgenerators.content.molds.MoldType;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.jesz.createdieselgenerators.packets.CDGExactVersionPayload;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.api.client.lang.FontHelper;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.ID;

@Mod(ID)
public class CreateDieselGenerators
{
    public static final String ID = "createdieselgenerators";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );
    public CreateDieselGenerators(IEventBus modEventBus, ModContainer container) {
        CDGExactVersionPayload.register(modEventBus, container.getModInfo().getVersion().toString());
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
        REGISTRATE.registerEventListeners(modEventBus);

        CDGItems.register();
        CDGBlocks.register();
        CDGFluids.register();
        CDGBlockEntityTypes.register();
        CDGEntityTypes.register();
        CDGSoundEvents.register(modEventBus);
        CDGRecipes.register(modEventBus);
        CDGMenuTypes.register();
        MoldType.register();
        CDGMountedStorageTypes.register();
        CDGCreativeTab.register(modEventBus);
        CDGPackets.register();
        CDGDataComponents.register(modEventBus);
        CDGDisplaySources.register();
        if (ModList.get().isLoaded("struts"))
            StrutYourStuffRegistryEntries.register();

        if (ModList.get().isLoaded("moonlight"))
            EveryCompatCompat.init();
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::register);

        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> CDGClient.init(modEventBus, container));
        container.registerConfig(ModConfig.Type.SERVER, CDGConfig.SERVER_SPEC, ID + "-server.toml");
        container.registerConfig(ModConfig.Type.COMMON, CDGConfig.COMMON_SPEC, ID + "-common.toml");
    }

    public static Identifier rl(String path){
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static Identifier id(String path) {
        return rl(path);
    }

    public static Component lang(String path, Object... args) {
        return Component.translatable(ID+"."+path, args);
    }
}
