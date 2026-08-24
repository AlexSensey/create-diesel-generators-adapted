package com.jesz.createdieselgenerators.foundation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.foundation.fluid.ResourceHandlerFluidAdapter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Keeps the add-on's 1.21 fluid tanks usable with NeoForge's transactional
 * resource capabilities introduced after 1.21.1.
 */
public final class FluidCompatibility {
    private FluidCompatibility() {}

    public static ResourceHandler<FluidResource> resourceHandler(IFluidHandler handler) {
        return resourceHandler(() -> handler);
    }

    public static ResourceHandler<FluidResource> resourceHandler(Supplier<IFluidHandler> handlerSupplier) {
        return new LegacyFluidResourceHandler(handlerSupplier);
    }

    public static CompoundTag writeBlockPos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    /**
     * Reads both the compound position format used by this port and the
     * three-int list used by older Catnip/NBTHelper versions.
     */
    public static BlockPos readBlockPos(CompoundTag parent, String key) {
        Tag value = parent.get(key);
        if (value instanceof CompoundTag pos)
            return new BlockPos(pos.getIntOr("X", 0), pos.getIntOr("Y", 0), pos.getIntOr("Z", 0));
        if (value instanceof ListTag list && list.size() >= 3)
            return new BlockPos(list.getInt(0).orElse(0), list.getInt(1).orElse(0), list.getInt(2).orElse(0));
        return BlockPos.ZERO;
    }

    public static IFluidHandler fluidHandler(ResourceHandler<FluidResource> handler) {
        return handler == null ? null : new ResourceHandlerFluidAdapter(handler);
    }

    public static IFluidHandler fluidHandler(ItemStack stack) {
        return fluidHandler(ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM));
    }

    public static CompoundTag writeTank(HolderLookup.Provider registries, FluidTank tank) {
        Tag encoded = FluidStack.OPTIONAL_CODEC.encodeStart(
                registries.createSerializationContext(NbtOps.INSTANCE), tank.getFluid())
            .result()
            .orElseGet(CompoundTag::new);
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();
    }

    public static void readTank(HolderLookup.Provider registries, CompoundTag tag, FluidTank tank) {
        FluidStack fluid = FluidStack.OPTIONAL_CODEC.parse(
                registries.createSerializationContext(NbtOps.INSTANCE), tag)
            .result()
            .orElse(FluidStack.EMPTY);
        tank.setFluid(fluid);
    }

    public static CompoundTag writeItems(HolderLookup.Provider registries, ItemStackHandler inventory) {
        CompoundTag result = new CompoundTag();
        result.putInt("Size", inventory.getSlots());
        var items = new net.minecraft.nbt.ListTag();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;
            CompoundTag encoded = ItemStack.CODEC.encodeStart(
                    registries.createSerializationContext(NbtOps.INSTANCE), stack)
                .result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .orElseGet(CompoundTag::new);
            encoded.putByte("Slot", (byte) slot);
            items.add(encoded);
        }
        result.put("Items", items);
        return result;
    }

    public static void readItems(HolderLookup.Provider registries, CompoundTag tag, ItemStackHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++)
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        for (Tag entry : tag.getListOrEmpty("Items")) {
            if (!(entry instanceof CompoundTag itemTag))
                continue;
            int slot = Byte.toUnsignedInt(itemTag.getByteOr("Slot", (byte) -1));
            if (slot >= inventory.getSlots())
                continue;
            ItemStack stack = ItemStack.CODEC.parse(
                    registries.createSerializationContext(NbtOps.INSTANCE), itemTag)
                .result()
                .orElse(ItemStack.EMPTY);
            inventory.setStackInSlot(slot, stack);
        }
    }

    private static final class LegacyFluidResourceHandler implements ResourceHandler<FluidResource> {
        private final Supplier<IFluidHandler> handlerSupplier;
        private final Journal journal = new Journal();

        private LegacyFluidResourceHandler(Supplier<IFluidHandler> handlerSupplier) {
            this.handlerSupplier = handlerSupplier;
        }

        private IFluidHandler handler() {
            return handlerSupplier.get();
        }

        @Override
        public int size() {
            return handler().getTanks();
        }

        @Override
        public FluidResource getResource(int index) {
            return FluidResource.of(handler().getFluidInTank(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return handler().getFluidInTank(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return !resource.isEmpty() && !isValid(index, resource) ? 0 : handler().getTankCapacity(index);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.isEmpty() || handler().isFluidValid(index, resource.toStack(1));
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !isValid(index, resource))
                return 0;
            IFluidHandler handler = handler();
            int inserted = handler.fill(resource.toStack(amount), FluidAction.SIMULATE);
            if (inserted <= 0)
                return 0;
            journal.updateSnapshots(transaction);
            return handler.fill(resource.toStack(inserted), FluidAction.EXECUTE);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0)
                return 0;
            IFluidHandler handler = handler();
            FluidStack extracted = handler.drain(resource.toStack(amount), FluidAction.SIMULATE);
            if (extracted.isEmpty())
                return 0;
            journal.updateSnapshots(transaction);
            return handler.drain(extracted, FluidAction.EXECUTE).getAmount();
        }

        private List<FluidStack> snapshot() {
            IFluidHandler handler = handler();
            List<FluidStack> result = new ArrayList<>(handler.getTanks());
            for (int i = 0; i < handler.getTanks(); i++)
                result.add(handler.getFluidInTank(i).copy());
            return result;
        }

        private final class Journal extends SnapshotJournal<List<FluidStack>> {
            @Override
            protected List<FluidStack> createSnapshot() {
                return snapshot();
            }

            @Override
            protected void revertToSnapshot(List<FluidStack> snapshot) {
                IFluidHandler handler = handler();
                if (handler instanceof FluidTank tank && !snapshot.isEmpty()) {
                    tank.setFluid(snapshot.getFirst());
                    return;
                }
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack current = handler.getFluidInTank(i);
                    if (!current.isEmpty())
                        handler.drain(current.copyWithAmount(current.getAmount()), FluidAction.EXECUTE);
                }
                for (FluidStack fluid : snapshot)
                    if (!fluid.isEmpty())
                        handler.fill(fluid.copy(), FluidAction.EXECUTE);
            }
        }
    }
}
