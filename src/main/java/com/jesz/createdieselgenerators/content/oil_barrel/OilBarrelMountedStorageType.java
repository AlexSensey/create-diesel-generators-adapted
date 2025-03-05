package com.jesz.createdieselgenerators.content.oil_barrel;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OilBarrelMountedStorageType extends MountedFluidStorageType<OilBarrelMountedStorage> {
    public OilBarrelMountedStorageType() {
        super(OilBarrelMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public OilBarrelMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof OilBarrelBlockEntity tank && tank.isController()) {
            return OilBarrelMountedStorage.fromTank(tank);
        }

        return null;
    }
}
