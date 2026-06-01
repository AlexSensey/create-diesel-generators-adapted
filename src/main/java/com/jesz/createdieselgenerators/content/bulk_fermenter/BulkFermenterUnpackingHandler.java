package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.impl.unpacking.BasinUnpackingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum BulkFermenterUnpackingHandler implements UnpackingHandler {
    INSTANCE;

    @Override
    public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items,
                          @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BulkFermenterBlockEntity fermenter))
            return false;

        fermenter.packagerMode = true;

        try {
            return UnpackingHandler.DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);
        } finally {
            fermenter.packagerMode = false;
        }
    }

    public static void register() {
        UnpackingHandler.REGISTRY.register(CDGBlocks.BULK_FERMENTER.get(), BulkFermenterUnpackingHandler.INSTANCE);
    }
}
