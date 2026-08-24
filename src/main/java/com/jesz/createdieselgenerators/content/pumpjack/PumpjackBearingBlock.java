package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PumpjackBearingBlock extends BearingBlock implements IBE<PumpjackBearingBlockEntity> {
    public PumpjackBearingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.isEmpty())
            return InteractionResult.PASS;
        if (!player.mayBuild())
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return InteractionResult.FAIL;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        withBlockEntityDo(level, pos, be -> {
            if (be.isRunning())
                be.disassemble();
            else
                be.assembleNextTick();
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return CDGBlocks.PUMPJACK_BEARING_B.getDefaultState().setValue(PumpjackBearingBBlock.FACING, originalState.getValue(FACING).getAxis() != Direction.Axis.Y ? originalState.getValue(FACING) : Direction.NORTH);
    }

    @Override
    public Class<PumpjackBearingBlockEntity> getBlockEntityClass() {
        return PumpjackBearingBlockEntity.class;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = context.getHorizontalDirection();
        if(context.getPlayer().isShiftKeyDown())
            return defaultBlockState().setValue(FACING, preferred.getOpposite());
        return defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public BlockEntityType<? extends PumpjackBearingBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.PUMPJACK_BEARING.get();
    }
}
