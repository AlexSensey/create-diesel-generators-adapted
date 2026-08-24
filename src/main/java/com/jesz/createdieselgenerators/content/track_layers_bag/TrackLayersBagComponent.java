package com.jesz.createdieselgenerators.content.track_layers_bag;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record TrackLayersBagComponent(ItemStack stack) implements ClientTooltipComponent, TooltipComponent {
    @Override
    public int getHeight(Font font) {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return (int) Math.ceil((double) stack.getCount() / 64) * 10 + 10;
    }
    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        for (int i = 0; i < stack.getCount(); i += 64) {
            graphics.item(stack, (int) (x + i / 6.4), y - (i % 5) / 2 + 1);
        }
        graphics.item(stack, x + (int) Math.ceil((float) stack.getCount() / 64) * 10 - 10, y);
        graphics.itemDecorations(font, stack, x + (int) Math.ceil((float) stack.getCount() / 64) * 10 - 10, y);
    }


}
