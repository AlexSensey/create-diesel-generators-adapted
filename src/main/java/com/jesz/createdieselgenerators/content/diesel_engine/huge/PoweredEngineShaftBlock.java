package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredEngineShaftBlock extends PoweredShaftBlock {
    public PoweredEngineShaftBlock(Properties properties) {
        super(properties);
    }

    public static BlockState getEquivalent(BlockState stateForPlacement) {
        if(stateForPlacement.getBlock() instanceof ShaftBlock)
            return CDGBlocks.POWERED_ENGINE_SHAFT.getDefaultState()
                    .setValue(PoweredShaftBlock.AXIS, stateForPlacement.getValue(ShaftBlock.AXIS))
                    .setValue(WATERLOGGED, stateForPlacement.getValue(WATERLOGGED));
        return stateForPlacement;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return stillValid(state, level, pos);
    }

    public static boolean stillValid(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == state.getValue(AXIS))
                continue;
            BlockPos enginePos = pos.relative(d, 2);
            BlockState engineState = level.getBlockState(enginePos);
            if (!(engineState.getBlock() instanceof HugeDieselEngineBlock))
                continue;
            if (!enginePos.relative(engineState.getValue(HugeDieselEngineBlock.FACING), 2).equals(pos))
                continue;
            return true;
        }
        return false;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.POWERED_ENGINE_SHAFT.get();
    }
}
