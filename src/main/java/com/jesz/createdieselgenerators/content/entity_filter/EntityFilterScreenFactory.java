package com.jesz.createdieselgenerators.content.entity_filter;

import com.tterrag.registrate.builders.MenuBuilder;

/** Loaded only when Registrate handles the client menu-screen registration event. */
public final class EntityFilterScreenFactory {
    private EntityFilterScreenFactory() {}

    public static MenuBuilder.ScreenFactory<EntityFilterMenu, EntityFilterScreen> create() {
        return EntityFilterScreen::new;
    }
}
