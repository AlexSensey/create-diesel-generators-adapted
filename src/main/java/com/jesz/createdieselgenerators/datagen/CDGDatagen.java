package com.jesz.createdieselgenerators.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.ponder.CDGPonderPlugin;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.Map;

public class CDGDatagen {
    public static void gatherData(GatherDataEvent event) {
        CreateDieselGenerators.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {

            JsonElement jsonElement = FilesHelper.loadJsonResource("assets/createdieselgenerators/lang/default/default.json");
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                provider.add(entry.getKey(), entry.getValue().getAsString());


            PonderIndex.addPlugin(new CDGPonderPlugin());
            PonderIndex.getLangAccess().provideLang(CreateDieselGenerators.ID, provider::add);
        });
    }


}
