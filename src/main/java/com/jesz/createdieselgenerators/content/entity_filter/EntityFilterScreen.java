package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.packets.EntityFilterScreenPacket;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterScreenPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        for (EntityAttribute entityAttribute : EntityAttribute.ALL)
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
        ClientNetworkHelper.INSTANCE.sendToServer(new EntityFilterScreenPacket(inverted ? FilterScreenPacket.Option.ADD_INVERTED_TAG : FilterScreenPacket.Option.ADD_TAG, attribute));
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
    protected void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        ItemStack stack = menu.ghostInventory.getStackInSlot(1);
        graphics.itemDecorations(font, stack, leftPos + 16, topPos + 62,
                String.valueOf(selectedAttributes.size() - 1));

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem() && hoveredSlot.index == 37)
            graphics.setComponentTooltipForNextFrame(font, selectedAttributes, mouseX, mouseY);
    }
    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack stackInSlot = menu.ghostInventory.getStackInSlot(0);
        if (!ItemStack.isSameItemSameComponents(stackInSlot, lastItemScanned))
            referenceItemChanged(stackInSlot);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (attributeSelector != null && attributeSelector.visible && attributeSelector.active
                && attributeSelector.isMouseOver(mouseX, mouseY)
                && attributeSelector.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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
        ClientNetworkHelper.INSTANCE.sendToServer(new EntityFilterScreenPacket(option, EntityAttribute.IS_MOB));
    }
}
