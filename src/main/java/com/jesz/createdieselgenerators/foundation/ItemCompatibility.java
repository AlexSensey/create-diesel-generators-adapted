package com.jesz.createdieselgenerators.foundation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class ItemCompatibility {
    private ItemCompatibility() {}

    public static ResourceHandler<ItemResource> resourceHandler(IItemHandlerModifiable handler) {
        return new LegacyItemResourceHandler(handler);
    }

    public static IItemHandler itemHandler(ResourceHandler<ItemResource> handler) {
        return handler == null ? null : new ResourceItemHandler(handler);
    }

    private static final class LegacyItemResourceHandler implements ResourceHandler<ItemResource> {
        private final IItemHandlerModifiable handler;
        private final List<SlotJournal> journals = new ArrayList<>();

        private LegacyItemResourceHandler(IItemHandlerModifiable handler) {
            this.handler = handler;
            for (int i = 0; i < handler.getSlots(); i++) journals.add(new SlotJournal(i));
        }
        public int size() { return handler.getSlots(); }
        public ItemResource getResource(int index) { return ItemResource.of(handler.getStackInSlot(index)); }
        public long getAmountAsLong(int index) { return handler.getStackInSlot(index).getCount(); }
        public long getCapacityAsLong(int index, ItemResource resource) { return !resource.isEmpty() && !isValid(index, resource) ? 0 : handler.getSlotLimit(index); }
        public boolean isValid(int index, ItemResource resource) { return resource.isEmpty() || handler.isItemValid(index, resource.toStack(1)); }
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            int inserted = amount - handler.insertItem(index, resource.toStack(amount), true).getCount();
            if (inserted <= 0) return 0;
            journals.get(index).updateSnapshots(transaction);
            handler.insertItem(index, resource.toStack(inserted), false);
            return inserted;
        }
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !resource.matches(handler.getStackInSlot(index))) return 0;
            ItemStack extracted = handler.extractItem(index, amount, true);
            if (extracted.isEmpty()) return 0;
            journals.get(index).updateSnapshots(transaction);
            handler.extractItem(index, extracted.getCount(), false);
            return extracted.getCount();
        }
        private final class SlotJournal extends SnapshotJournal<ItemStack> {
            private final int slot;
            private SlotJournal(int slot) { this.slot = slot; }
            protected ItemStack createSnapshot() { return handler.getStackInSlot(slot).copy(); }
            protected void revertToSnapshot(ItemStack snapshot) { handler.setStackInSlot(slot, snapshot); }
        }
    }

    private record ResourceItemHandler(ResourceHandler<ItemResource> handler) implements IItemHandler {
        public int getSlots() { return handler.size(); }
        public ItemStack getStackInSlot(int slot) { return ItemUtil.getStack(handler, slot); }
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return ItemUtil.insertItemReturnRemaining(handler, slot, stack, simulate, null); }
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            ItemResource resource = handler.getResource(slot);
            if (resource.isEmpty()) return ItemStack.EMPTY;
            try (Transaction transaction = Transaction.openRoot()) {
                int extracted = handler.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), transaction);
                if (!simulate) transaction.commit();
                return resource.toStack(extracted);
            }
        }
        public int getSlotLimit(int slot) { return handler.getCapacityAsInt(slot, ItemResource.EMPTY); }
        public boolean isItemValid(int slot, ItemStack stack) { return handler.isValid(slot, ItemResource.of(stack)); }
    }
}
