package com.jesz.createdieselgenerators.content.turret;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;

/**
 * The old entity-layer injection API was removed in 26.2. The gameplay link
 * between an operator and a turret remains active; its cosmetic hat will be
 * registered through the new render-state pipeline in a later compatibility
 * pass.
 */
public final class TurretOperatorHatLayer {
    private TurretOperatorHatLayer() {}

    public static void registerOnAll(EntityRenderDispatcher dispatcher) {
    }
}
