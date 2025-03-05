package com.jesz.createdieselgenerators.content.fluid_coupling;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FluidCouplingBlock extends DirectionalKineticBlock implements IBE<FluidCouplingBlockEntity> {
    public static final BooleanProperty INPUT = BooleanProperty.create("input");
    public FluidCouplingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if(state.getBlock() instanceof FluidCouplingBlock && state.getValue(FACING) == context.getClickedFace().getOpposite())
            return defaultBlockState().setValue(FACING, state.getValue(FACING).getOpposite());
        return super.getStateForPlacement(context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INPUT);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collision) {
        return AllShapes.CASING_11PX.get(state.getValue(FACING));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moved) {
        withBlockEntityDo(level, pos, FluidCouplingBlockEntity::updateCoupledBE);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        context.getLevel().setBlock(context.getClickedPos(), state.setValue(INPUT, !state.getValue(INPUT)), 3);
        IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), FluidCouplingBlockEntity::updateCoupledBE);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING) == face;
    }

    @Override
    public Class<FluidCouplingBlockEntity> getBlockEntityClass() {
        return FluidCouplingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidCouplingBlockEntity> getBlockEntityType() {
        return null;
//        return CDGBlockEntityTypes.FLUID_COUPLING.get();
    }
}
