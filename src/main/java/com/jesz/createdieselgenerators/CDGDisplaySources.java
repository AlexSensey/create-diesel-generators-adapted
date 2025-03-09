package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.diesel_engine.EngineStateDisplaySource;
import com.jesz.createdieselgenerators.content.pumpjack.OilAmountDisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGDisplaySources {
    public static final RegistryEntry<OilAmountDisplaySource> OIL_AMOUNT = REGISTRATE.displaySource("oil_amount", OilAmountDisplaySource::new).register();
    public static final RegistryEntry<EngineStateDisplaySource> ENGINE_STATE = REGISTRATE.displaySource("diesel_engine", EngineStateDisplaySource::new).register();
}
