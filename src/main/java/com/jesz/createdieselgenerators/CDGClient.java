package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

/** Client bootstrap, loaded only through Catnip's client-side executor. */
public final class CDGClient {
    private CDGClient() {}

    public static void init(IEventBus modEventBus, ModContainer container) {
        CDGPartialModels.init();
        container.registerConfig(ModConfig.Type.CLIENT, CDGConfig.CLIENT_SPEC,
                CreateDieselGenerators.ID + "-client.toml");
        LighterModel.initSkins();
    }
}
