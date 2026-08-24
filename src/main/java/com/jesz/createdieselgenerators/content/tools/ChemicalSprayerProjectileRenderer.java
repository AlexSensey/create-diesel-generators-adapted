package com.jesz.createdieselgenerators.content.tools;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

public class ChemicalSprayerProjectileRenderer extends EntityRenderer<ChemicalSprayerProjectileEntity, EntityRenderState> {
    public ChemicalSprayerProjectileRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
