package com.jesz.createdieselgenerators.content.concrete;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.ticks.TickPriority;

public class ConcreteEncasedFluidPipeBlock extends EncasedPipeBlock {
    public ConcreteEncasedFluidPipeBlock(Properties properties) {
        super(properties, AllBlocks.ANDESITE_CASING::get);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getPlayer() != null && !context.getPlayer().isCreative())
            return InteractionResult.PASS;
        context.getLevel().setBlock(context.getClickedPos(), state.cycle(PipeBlock.PROPERTY_BY_DIRECTION.get(context.getClickedFace())), 3);
        IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
                                boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(world, pos);
        Direction d = fixedValidateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(d)))
            return;
        world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    public static Direction fixedValidateNeighbourChange(BlockState state, Level world, BlockPos pos, Block otherBlock,
                                                    BlockPos neighborPos, boolean isMoving) {
        if (world.isClientSide)
            return null;
        // calling getblockstate() as otherBlock param seems to contain the block which
        // was replaced
        otherBlock = world.getBlockState(neighborPos)
                .getBlock();
        if (otherBlock instanceof FluidPipeBlock)
            return null;
        if (otherBlock instanceof AxisPipeBlock)
            return null;
        if (otherBlock instanceof PumpBlock)
            return null;
        if (otherBlock instanceof LiquidBlock)
            return null;
        for (Direction d : Iterate.directions) {
            if (!pos.relative(d)
                    .equals(neighborPos))
                continue;
            return d;
        }
        return null;
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.CONCRETE_ENCASED_FLUID_PIPE.get();
    }
}
