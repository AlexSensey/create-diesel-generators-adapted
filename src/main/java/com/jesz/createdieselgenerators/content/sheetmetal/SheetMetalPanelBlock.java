package com.jesz.createdieselgenerators.content.sheetmetal;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SheetMetalPanelBlock extends Block implements SimpleWaterloggedBlock, IWrenchable {
    public static BooleanProperty ROLL = BooleanProperty.create("roll");
    public static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static DirectionProperty FACING = BlockStateProperties.FACING;
    public SheetMetalPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(ROLL, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(ROLL, context.getNearestLookingDirection().getAxis().isVertical() && context.getHorizontalDirection().getAxis() == Direction.Axis.X);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.CASING_2PX.get(state.getValue(FACING));
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
        return Shapes.empty();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, ROLL);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        KineticBlockEntity.switchToBlockState(level, context.getClickedPos(), updateAfterWrenched(state.setValue(ROLL, !state.getValue(ROLL)), context));

        BlockEntity be = context.getLevel()
                .getBlockEntity(context.getClickedPos());
        if (be instanceof GeneratingKineticBlockEntity) {
            ((GeneratingKineticBlockEntity) be).reActivateSource = true;
        }

        if (level.getBlockState(context.getClickedPos()) != state)
            IWrenchable.playRotateSound(level, context.getClickedPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING))).setValue(ROLL,
                state.getValue(FACING).getAxis().isVertical() ? state.getValue(ROLL) ^ (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) : state.getValue(ROLL));
    }
}
