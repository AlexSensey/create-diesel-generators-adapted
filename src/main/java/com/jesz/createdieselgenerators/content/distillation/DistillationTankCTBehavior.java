package com.jesz.createdieselgenerators.content.distillation;

import com.jesz.createdieselgenerators.CDGSpriteShifts;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DistillationTankCTBehavior extends ConnectedTextureBehaviour.Base {

    @Override
    public @Nullable CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        if(direction.getAxis().isVertical())
            return CDGSpriteShifts.DISTILLATION_TANK_TOP;
        if(direction == Direction.NORTH)
            return CDGSpriteShifts.DISTILLATION_TANK_NORTH;
        return CDGSpriteShifts.DISTILLATION_TANK;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        if(pos.above(1).equals(otherPos))
            return !state.getValue(DistillationTankBlock.TOP);
        if(pos.below(1).equals(otherPos))
            return !state.getValue(DistillationTankBlock.BOTTOM);
        return other.getBlock() instanceof DistillationTankBlock && ConnectivityHandler.isConnected(reader, pos, otherPos);

    }
}
