package com.jesz.createdieselgenerators.mixin_interfaces;

import net.minecraft.core.BlockPos;

public interface IEntity {
    BlockPos getTurretPos();
    void setTurretPos(BlockPos pos);
}
