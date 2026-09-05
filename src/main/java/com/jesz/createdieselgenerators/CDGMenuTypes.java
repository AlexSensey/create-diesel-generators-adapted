package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public final class CDGMenuTypes {
    private static final String ENTITY_FILTER_CLIENT_FACTORY =
            "com.jesz.createdieselgenerators.content.entity_filter.EntityFilterScreenFactory";

    public static final MenuEntry<EntityFilterMenu> ENTITY_FILTER = register(
            "entity_filter", EntityFilterMenu::new, ENTITY_FILTER_CLIENT_FACTORY);

    private CDGMenuTypes() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <M extends net.minecraft.world.inventory.AbstractContainerMenu> MenuEntry<M> register(
            String name, MenuBuilder.ForgeMenuFactory<M> menuFactory, String clientFactoryClass) {
        NonNullSupplier screenFactory = () -> {
            try {
                return Class.forName(clientFactoryClass).getMethod("create").invoke(null);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Could not create client menu factory " + clientFactoryClass,
                        exception);
            }
        };
        return (MenuEntry<M>) CreateDieselGenerators.REGISTRATE
                .menu(name, menuFactory, screenFactory)
                .register();
    }

    public static void register() {
    }
}
