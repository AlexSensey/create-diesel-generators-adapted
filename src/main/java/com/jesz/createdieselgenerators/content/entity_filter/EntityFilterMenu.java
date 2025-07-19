package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.AbstractFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class EntityFilterMenu extends AbstractFilterMenu {

    public AttributeFilterWhitelistMode whitelistMode;
    List<EntityAttribute.EntityAttributeEntry> selectedAttributes;

    public EntityFilterMenu(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    protected EntityFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
    }

    public void appendSelectedAttribute(EntityAttribute entry, Boolean inverted) {
        selectedAttributes.add(new EntityAttribute.EntityAttributeEntry(entry, inverted));
    }

    @Override
    protected void init(Inventory inv, ItemStack contentHolder) {
        super.init(inv, contentHolder);
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Selected Tags").withStyle(ChatFormatting.RESET, ChatFormatting.BLUE));
        ghostInventory.setStackInSlot(1, stack);
    }
    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(2);
    }

    @Override
    protected int getPlayerInventoryXOffset() {
        return 51;
    }

    @Override
    protected int getPlayerInventoryYOffset() {
        return 107;
    }

    @Override
    protected void addFilterSlots() {
        this.addSlot(new SlotItemHandler(ghostInventory, 0, 16, 27));
        this.addSlot(new SlotItemHandler(ghostInventory, 1, 16, 62) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }
        });
    }

    @Override
    public void clearContents() {
        selectedAttributes.clear();
    }
    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == 37)
            return;
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean canDragTo(Slot slotIn) {
        if (slotIn.index == 37)
            return false;
        return super.canDragTo(slotIn);
    }
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
        if (slotIn.index == 37)
            return false;
        return super.canTakeItemForPickAll(stack, slotIn);
    }
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (index == 37)
            return ItemStack.EMPTY;
        if (index == 36) {
            ghostInventory.setStackInSlot(37, ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }
        if (index < 36) {
            ItemStack stackToInsert = playerInventory.getItem(index);
            ItemStack copy = stackToInsert.copy();
            copy.setCount(1);
            ghostInventory.setStackInSlot(0, copy);
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void initAndReadInventory(ItemStack filterItem) {
        super.initAndReadInventory(filterItem);
        selectedAttributes = new ArrayList<>();
        whitelistMode = filterItem.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
        selectedAttributes = new ArrayList<>(EntityFilterItem.getEntries(filterItem));
    }

    @Override
    protected void saveData(ItemStack filterItem) {

        filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, whitelistMode);
//        ListTag attributes = new ListTag();
//        selectedAttributes.forEach(at -> {
//            if (at == null)
//                return;
//            if(at.getFirst() == null)
//                return;
//            CompoundTag tag = at.getFirst().write();
//            tag.putBoolean("Inverted", at.getSecond());
//            attributes.add(tag);
//        });
//        filterItem.getTag()
//                .put("MatchedAttributes", attributes);
//
//        if (attributes.isEmpty() && whitelist == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ)
//            filterItem.setTag(null);

        List<EntityAttribute.EntityAttributeEntry> attributes = new ArrayList<>();

        selectedAttributes.forEach(at -> {
            if (at == null)
                return;
            attributes.add(at);
        });
        filterItem.set(CDGDataComponents.ENTITY_FILTER_MATCHED_ATTRIBUTES, attributes);

        if (attributes.isEmpty() && whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ) {
            filterItem.remove(CDGDataComponents.ENTITY_FILTER_MATCHED_ATTRIBUTES);
            filterItem.remove(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
        }
    }
}
