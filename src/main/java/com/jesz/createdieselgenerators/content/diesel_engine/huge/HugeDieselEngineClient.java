package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;

/** Client-side animation calculations kept out of the shared block entity class. */
public final class HugeDieselEngineClient {
    private HugeDieselEngineClient() {}

    public static Float getTargetAngle(HugeDieselEngineBlockEntity engine) {
        BlockState state = engine.getBlockState();
        if (!CDGBlocks.HUGE_DIESEL_ENGINE.has(state))
            return null;

        Direction facing = state.getValue(FACING);
        PoweredEngineShaftBlockEntity shaft = engine.getShaft();
        if (shaft == null)
            return null;

        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        float angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getBlockPos(), axis);
        if (axis == facingAxis)
            return null;
        if (axis.isHorizontal()
                && (facingAxis == Direction.Axis.X
                ^ facing.getAxisDirection() == Direction.AxisDirection.POSITIVE))
            angle *= -1;
        if (axis == Direction.Axis.X && facing == Direction.DOWN)
            angle *= -1;
        return angle;
    }
}
