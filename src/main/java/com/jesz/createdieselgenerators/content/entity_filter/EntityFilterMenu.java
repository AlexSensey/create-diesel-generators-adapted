package com.jesz.createdieselgenerators.content.entity_filter;

import com.simibubi.create.content.logistics.filter.AbstractFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class EntityFilterMenu extends AbstractFilterMenu {

    public AttributeFilterMenu.WhitelistMode whitelist;
    List<Pair<EntityAttribute, Boolean>> selectedAttributes;

    public EntityFilterMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
    }

    protected EntityFilterMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
        super(type, id, inv, contentHolder);
    }
    public void appendSelectedAttribute(EntityAttribute entry, Boolean inverted) {
        selectedAttributes.add(Pair.of(entry, inverted));
    }
    @Override
    protected void init(Inventory inv, ItemStack contentHolder) {
        super.init(inv, contentHolder);
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        stack.setHoverName(
                Component.literal("Selected Tags").withStyle(ChatFormatting.RESET, ChatFormatting.BLUE));
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
        whitelist = AttributeFilterMenu.WhitelistMode.values()[filterItem.getOrCreateTag()
                .getInt("Whitelist")];
        ListTag attributes = filterItem.getOrCreateTag()
                .getList("MatchedAttributes", Tag.TAG_COMPOUND);
        attributes.forEach(nbt -> {
            CompoundTag compound = (CompoundTag) nbt;
            EntityAttribute fEntry = EntityAttribute.fromNBT(compound);
            if(fEntry == null)
                return;
            selectedAttributes.add(Pair.of(fEntry, compound.getBoolean("Inverted")));
        });
    }

    @Override
    protected void saveData(ItemStack filterItem) {
        filterItem.getOrCreateTag()
                .putInt("Whitelist", whitelist.ordinal());
        ListTag attributes = new ListTag();
        selectedAttributes.forEach(at -> {
            if (at == null)
                return;
            if(at.getFirst() == null)
                return;
            CompoundTag tag = at.getFirst().write();
            tag.putBoolean("Inverted", at.getSecond());
            attributes.add(tag);
        });
        filterItem.getTag()
                .put("MatchedAttributes", attributes);

        if (attributes.isEmpty() && whitelist == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ)
            filterItem.setTag(null);
    }
}
