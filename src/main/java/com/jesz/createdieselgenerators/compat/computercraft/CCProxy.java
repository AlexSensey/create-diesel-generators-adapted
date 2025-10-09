package com.jesz.createdieselgenerators.compat.computercraft;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.compat.computercraft.FallbackComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import java.util.function.Function;

public class CCProxy {
    public static void register() {
        fallbackFactory = FallbackComputerBehaviour::new;
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CCProxy::registerWithDependency);
    }

    private static void registerWithDependency() {
        computerFactory = CDGComputerBehaviour::new;
    }

    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> fallbackFactory;
    private static Function<SmartBlockEntity, ? extends AbstractComputerBehaviour> computerFactory;

    public static AbstractComputerBehaviour behaviour(SmartBlockEntity sbe) {
        fallbackFactory = FallbackComputerBehaviour::new;

        if (computerFactory == null)
            return fallbackFactory.apply(sbe);
        return computerFactory.apply(sbe);
    }

}
