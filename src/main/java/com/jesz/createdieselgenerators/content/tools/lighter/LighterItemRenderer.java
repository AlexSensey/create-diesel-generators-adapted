package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class LighterItemRenderer extends CustomRenderedItemModelRenderer {

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        LighterState lighterState = stack.getOrDefault(CDGDataComponents.LIGHTER_STATE, LighterState.CLOSED);
        LighterSkinEntry lighterSkinEntry = LighterModel.lighterSkinModels.get(LighterModel.lighterSkinIDs.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT)));
        if (lighterSkinEntry != null) {
            if (lighterState == LighterState.CLOSED)
                renderer.render(lighterSkinEntry.closedModel().get(), light);
            else if (lighterState == LighterState.OPEN)
                renderer.render(lighterSkinEntry.openModel().get(), light);
            else
                renderer.render(lighterSkinEntry.ignitedModel().get(), light);
            return;
        }
        LighterSkinEntry standardEntry = LighterSkinEntry.STANDARD;
        if (lighterState == LighterState.CLOSED)
            renderer.render(standardEntry.closedModel().get(), light);
        else if (lighterState == LighterState.OPEN)
            renderer.render(standardEntry.openModel().get(), light);
        else
            renderer.render(standardEntry.ignitedModel().get(), light);
    }
}
