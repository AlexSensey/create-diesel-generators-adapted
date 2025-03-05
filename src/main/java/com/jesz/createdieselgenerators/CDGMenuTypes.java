package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterMenu;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterScreen;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class CDGMenuTypes {
    public static final MenuEntry<EntityFilterMenu> ENTITY_FILTER =
            register("entity_filter", EntityFilterMenu::new, () -> EntityFilterScreen::new);
    static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> MenuEntry<M> register(String name, MenuBuilder.ForgeMenuFactory<M> factory, NonNullSupplier<MenuBuilder.ScreenFactory<M, S>> screenFactory){
        return CreateDieselGenerators.REGISTRATE.menu(name, factory, screenFactory).register();
    }

    public static void register() {}

}
