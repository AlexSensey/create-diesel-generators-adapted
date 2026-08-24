package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import java.util.Locale;

public class LighterItemRenderer extends CustomRenderedItemModelRenderer {

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        LighterState lighterState = stack.getOrDefault(CDGDataComponents.LIGHTER_STATE, LighterState.CLOSED);
        LighterSkinEntry lighterSkinEntry = LighterModel.lighterSkinModels.get(LighterModel.lighterSkinIDs.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT)));
        if (lighterSkinEntry != null) {
            if (lighterState == LighterState.CLOSED)
                renderPartial(lighterSkinEntry.closedModel(), ms, buffer, light);
            else if (lighterState == LighterState.OPEN)
                renderPartial(lighterSkinEntry.openModel(), ms, buffer, light);
            else
                renderPartial(lighterSkinEntry.ignitedModel(), ms, buffer, light);
            return;
        }
        LighterSkinEntry standardEntry = LighterSkinEntry.STANDARD;
        if (lighterState == LighterState.CLOSED)
            renderPartial(standardEntry.closedModel(), ms, buffer, light);
        else if (lighterState == LighterState.OPEN)
            renderPartial(standardEntry.openModel(), ms, buffer, light);
        else
            renderPartial(standardEntry.ignitedModel(), ms, buffer, light);
    }

    private static void renderPartial(LighterModel model, PoseStack ms, MultiBufferSource buffer, int light) {
        com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility
                .partial(model.get(), Blocks.AIR.defaultBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }
}
