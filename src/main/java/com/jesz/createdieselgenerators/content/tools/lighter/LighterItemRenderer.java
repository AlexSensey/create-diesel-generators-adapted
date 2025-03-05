package com.jesz.createdieselgenerators.content.tools.lighter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class LighterItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        try {
            if (stack.getTag() != null && LighterModel.lighterSkinIDs.containsKey(stack.getHoverName().getString().toLowerCase(Locale.ROOT))) {
                if (LighterModel.lighterSkinModels.containsKey(LighterModel.lighterSkinIDs.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT)))) {
                    if (stack.getTag().getInt("Type") == 0)
                        renderer.render(LighterModel.lighterSkinModels.get(LighterModel.lighterSkinIDs.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT))).closedModel().get(), light);
                    else if (stack.getTag().getInt("Type") == 1)
                        renderer.render(LighterModel.lighterSkinModels.get(LighterModel.lighterSkinIDs.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT))).openModel().get(), light);
                    else
                        renderer.render(LighterModel.lighterSkinModels.get(LighterModel.lighterSkinIDs.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT))).ignitedModel().get(), light);
                    return;
                }
            }
            if (stack.getTag() == null || stack.getTag().getInt("Type") == 0)
                renderer.render(LighterModel.lighterSkinModels.get("standard").closedModel().get(), light);
            else if (stack.getTag().getInt("Type") == 1)
                renderer.render(LighterModel.lighterSkinModels.get("standard").openModel().get(), light);
            else
                renderer.render(LighterModel.lighterSkinModels.get("standard").ignitedModel().get(), light);
        }catch (NullPointerException e) {
            renderer.render(model.getOriginalModel(), light);
        }
    }
}
