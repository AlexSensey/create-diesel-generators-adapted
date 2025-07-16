package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.pumpjack.OilAmountDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGDisplaySources {
    public static final RegistryEntry<DisplaySource, OilAmountDisplaySource> OIL_AMOUNT = REGISTRATE.displaySource("oil_amount", OilAmountDisplaySource::new).register();
}
