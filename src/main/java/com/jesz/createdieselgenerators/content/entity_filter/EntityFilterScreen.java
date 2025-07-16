package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.packets.CDGPackets;
import com.jesz.createdieselgenerators.packets.EntityFilterScreenPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class EntityFilterScreen extends AbstractFilterScreen<EntityFilterMenu> {
    IconButton whitelistCon, whitelistDis, blacklist, add, addInverted;
    Indicator whitelistConIndicator, whitelistDisIndicator, blacklistIndicator;
    SelectionScrollInput attributeSelector;
    List<Component> selectedAttributes = new ArrayList<>();
    List<EntityAttribute> attributesOfItem = new ArrayList<>();
    Label attributeSelectorLabel;
    ItemStack lastItemScanned = ItemStack.EMPTY;
    public EntityFilterScreen(EntityFilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 7);
        super.init();
        whitelistDis = new IconButton(leftPos + 38, topPos + 61, AllIcons.I_WHITELIST_OR);
        whitelistCon = new IconButton(leftPos + 56, topPos + 61, AllIcons.I_WHITELIST_AND);
        blacklist = new IconButton(leftPos + 74, topPos + 61, AllIcons.I_WHITELIST_NOT);

        whitelistCon.withCallback(() -> {
            menu.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
            sendOptionUpdate(FilterScreenPacket.Option.WHITELIST2);
        });
        whitelistCon.setToolTip(CreateDieselGenerators.lang("gui.entity_filter.allow_list_conjunctive"));
        whitelistDis.withCallback(() -> {
            menu.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
            sendOptionUpdate(FilterScreenPacket.Option.WHITELIST);
        });
        whitelistDis.setToolTip(CreateDieselGenerators.lang("gui.entity_filter.allow_list_disjunctive"));

        blacklist.withCallback(() -> {
            menu.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
            sendOptionUpdate(FilterScreenPacket.Option.BLACKLIST);
        });
        blacklist.setToolTip(CreateDieselGenerators.lang("gui.entity_filter.deny_list"));

        addRenderableWidgets(whitelistCon, whitelistDis, blacklist);

        addRenderableWidget(add = new IconButton(leftPos + 182, topPos + 26, AllIcons.I_ADD));
        addRenderableWidget(addInverted = new IconButton(leftPos + 200, topPos + 26, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
        add.withCallback(() -> handleAddedAttribute(false));
        add.setToolTip(CreateDieselGenerators.lang("gui.entity_filter.add_attribute"));
        addInverted.withCallback(() -> handleAddedAttribute(true));
        addInverted.setToolTip(CreateDieselGenerators.lang("gui.entity_filter.add_inverted_attribute"));

        handleIndicators();

        attributeSelectorLabel = new Label(leftPos + 43, topPos + 31, Component.empty()).colored(0xF3EBDE)
                .withShadow();
        attributeSelector = new SelectionScrollInput(leftPos + 39, topPos + 26, 137, 18);
        attributeSelector.forOptions(Arrays.asList(Component.empty()));
        attributeSelector.removeCallback();
        referenceItemChanged(menu.ghostInventory.getStackInSlot(0));

        addRenderableWidget(attributeSelector);
        addRenderableWidget(attributeSelectorLabel);

        selectedAttributes.clear();
        selectedAttributes.add((menu.selectedAttributes.isEmpty() ?
                CreateDieselGenerators.lang("gui.entity_filter.no_selected_attributes") :
                CreateDieselGenerators.lang("gui.entity_filter.selected_attributes")).plainCopy()
                .withStyle(ChatFormatting.YELLOW));
        menu.selectedAttributes.forEach(at -> selectedAttributes.add(Component.literal("- ")
                .append(at.attribute().format(at.inverted()))
                .withStyle(ChatFormatting.GRAY)));
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);
//        font.draw(ms, title, x + (background.width - 8) / 2 - font.width(title) / 2, y + 4,
//                CDGItems.ENTITY_FILTER.isIn(menu.contentHolder) ? 0x303030 : 0x592424);

        GuiGameElement.of(menu.contentHolder).<GuiGameElement
                        .GuiRenderBuilder>at(x + background.getWidth() + 8, y + background.getHeight() - 52, -200)
                .scale(4)
                .render(graphics);
    }
    private void referenceItemChanged(ItemStack stack) {
        lastItemScanned = stack;
        if (stack.isEmpty()) {
            attributeSelector.active = false;
            attributeSelector.visible = false;
            attributeSelectorLabel.text = CreateDieselGenerators.lang("gui.entity_filter.add_reference_item").plainCopy()
                    .withStyle(ChatFormatting.ITALIC);
            add.active = false;
            addInverted.active = false;
            attributeSelector.calling(s -> {
            });
            return;
        }

        add.active = true;

        addInverted.active = true;
        attributeSelector.titled(stack.getHoverName()
                .plainCopy()
                .append("..."));
        attributesOfItem.clear();
        for (EntityAttribute entityAttribute : EntityAttribute.all)
            attributesOfItem.addAll(entityAttribute.listAttributesOf(stack));
        List<Component> options = attributesOfItem.stream()
                .map(a -> a.format(false))
                .collect(Collectors.toList());
        attributeSelector.forOptions(options);
        attributeSelector.active = true;
        attributeSelector.visible = true;
        attributeSelector.setState(0);
        attributeSelector.calling(i -> {
            if(options.isEmpty())
                return;
            attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
            EntityAttribute selected = attributesOfItem.get(i);
            for (EntityAttribute.EntityAttributeEntry existing : menu.selectedAttributes) {
                CompoundTag testTag = existing.attribute()
                                    .write();
                CompoundTag testTag2 = selected.write();
                if (testTag.equals(testTag2)) {
                    add.active = false;
                    addInverted.active = false;
                    return;
                }
            }
            add.active = true;
            addInverted.active = true;
        });
        attributeSelector.onChanged();
    }
    protected boolean handleAddedAttribute(boolean inverted) {
        int index = attributeSelector.getState();
        if (index >= attributesOfItem.size())
            return false;
        add.active = false;
        addInverted.active = false;
        EntityAttribute attribute = attributesOfItem.get(index);
        CatnipServices.NETWORK.sendToServer(new EntityFilterScreenPacket(inverted ? FilterScreenPacket.Option.ADD_INVERTED_TAG : FilterScreenPacket.Option.ADD_TAG, attribute));
        menu.appendSelectedAttribute(attribute, inverted);
        if (menu.selectedAttributes.size() == 1)
            selectedAttributes.set(0,
                    CreateDieselGenerators.lang("gui.entity_filter.selected_attributes").plainCopy()
                    .withStyle(ChatFormatting.YELLOW));
        selectedAttributes.add(Component.literal("- ").append(attribute.format(inverted))
                .withStyle(ChatFormatting.GRAY));
        return true;
    }
    @Override
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ItemStack stack = menu.ghostInventory.getStackInSlot(1);
        PoseStack matrixStack = graphics.pose();
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 150);
        graphics.renderItemDecorations(font, stack, leftPos + 16, topPos + 62,
                String.valueOf(selectedAttributes.size() - 1));
        matrixStack.popPose();;

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.hoveredSlot.index == 37) {
                graphics.renderComponentTooltip(font, selectedAttributes, mouseX, mouseY);
                return;
            }
            graphics.renderTooltip(font, this.hoveredSlot.getItem(), mouseX, mouseY);
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack stackInSlot = menu.ghostInventory.getStackInSlot(0);
        if (!ItemStack.isSameItemSameComponents(stackInSlot, lastItemScanned))
            referenceItemChanged(stackInSlot);
    }
    @Override
    protected void contentsCleared() {
        selectedAttributes.clear();
        selectedAttributes.add(CreateDieselGenerators.lang("gui.entity_filter.no_selected_attributes").plainCopy()
                .withStyle(ChatFormatting.YELLOW));
        if (!lastItemScanned.isEmpty()) {
            add.active = true;
            addInverted.active = true;
        }
    }
    @Override
    protected boolean isButtonEnabled(IconButton button) {
        if (button == blacklist)
            return menu.whitelistMode != AttributeFilterWhitelistMode.BLACKLIST;
        if (button == whitelistCon)
            return menu.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_CONJ;
        if (button == whitelistDis)
            return menu.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_DISJ;
        return true;
    }
    @Override
    protected List<IconButton> getTooltipButtons() {
        return Arrays.asList(blacklist, whitelistCon, whitelistDis);
    }

    @Override
    protected List<MutableComponent> getTooltipDescriptions() {
        return Arrays.asList(CreateDieselGenerators.lang("gui.entity_filter.deny_list.description").plainCopy(),
                CreateDieselGenerators.lang("gui.entity_filter.allow_list_conjunctive.description").plainCopy(),
                CreateDieselGenerators.lang("gui.entity_filter.allow_list_disjunctive.description").plainCopy());
    }

    @Override
    protected void sendOptionUpdate(FilterScreenPacket.Option option) {
        CatnipServices.NETWORK.sendToServer(new EntityFilterScreenPacket(option, EntityAttribute.IS_MOB));
    }
}
