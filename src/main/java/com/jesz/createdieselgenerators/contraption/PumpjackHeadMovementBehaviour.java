package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.jesz.createdieselgenerators.foundation.FluidCompatibility;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

/** Shared pumpjack actor logic. Rendering lives in {@link PumpjackHeadMovementBehaviourClient}. */
public class PumpjackHeadMovementBehaviour implements MovementBehaviour {
    private BlockPos holePos;
    private BlockPos headPos;

    @Nullable
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public boolean isActive(MovementContext context) {
        if (!(context.contraption instanceof BearingContraption bearing))
            return false;
        if (bearing.getFacing().getAxis() == Direction.Axis.Y
                || context.state.getValue(PumpjackBearingBBlock.FACING).getAxis()
                != bearing.getFacing().getClockWise().getAxis())
            return false;
        return context.world.getBlockEntity(
                context.contraption.anchor.relative(bearing.getFacing().getOpposite()))
                instanceof PumpjackBearingBlockEntity;
    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        if (!(context.contraption instanceof BearingContraption bearingContraption))
            return;
        if (!(context.world.getBlockEntity(context.contraption.anchor.relative(
                bearingContraption.getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity bearing))
            return;

        headPos = context.contraption.anchor.offset(context.localPos);
        holePos = headPos;
        for (int i = 0; i < 32; i++) {
            if (context.world.getBlockEntity(holePos) instanceof PumpjackHoleBlockEntity)
                break;
            holePos = holePos.below();
        }

        if (context.world.getBlockEntity(holePos) instanceof PumpjackHoleBlockEntity hole
                && bearing.crankSpeed >= 8) {
            boolean xAxis = bearing.getBlockState().getValue(FACING).getAxis() == Direction.Axis.X;
            hole.headPos = xAxis ? context.localPos.getZ() : context.localPos.getX();
            hole.bearingPos = xAxis ? bearing.bearingBPos.getZ() : bearing.bearingBPos.getX();
            if ((bearing.crankAngle + 270) % 360
                    < (context.data.getFloatOr("OldCrankAngle", 0) + 270) % 360)
                hole.pumpjackRotation(bearing.isLarge);
        }
        context.data.putFloat("OldCrankAngle", bearing.crankAngle);
        context.data.put("HolePos", FluidCompatibility.writeBlockPos(holePos));
    }
}
