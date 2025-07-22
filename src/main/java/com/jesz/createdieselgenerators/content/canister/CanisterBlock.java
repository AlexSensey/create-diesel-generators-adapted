package com.jesz.createdieselgenerators.content.canister;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;

public class CanisterBlock extends Block implements IBE<CanisterBlockEntity>, ProperWaterloggedBlock, IWrenchable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENCHANTED = BooleanProperty.create("enchanted");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CanisterBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState()
                        .setValue(WATERLOGGED, false)
                        .setValue(ENCHANTED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(ENCHANTED) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER)), context);
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide)
            return;

        withBlockEntityDo(level, pos, be -> {
            be.setComponentPatch(stack.getComponentsPatch());
            be.setCapacityEnchantLevel(stack.getEnchantmentLevel(level.holderOrThrow(AllEnchantments.CAPACITY)));
        });

        if (stack.getEnchantmentLevel(level.holderOrThrow(AllEnchantments.CAPACITY)) != 0)
            level.setBlock(pos, state.setValue(ENCHANTED, true), 2);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(state.getValue(FACING).getAxis() == Direction.Axis.Y)
            return Block.box(2, 0, 2, 14, 16, 14);
        else if(state.getValue(FACING).getAxis() == Direction.Axis.X)
            return Block.box(0, 2, 2, 16, 14, 14);
        else
            return Block.box(2, 2, 0, 14, 14, 16);

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED, ENCHANTED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        updateWater(level, state, pos);
        return state;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> lootDrops = super.getDrops(state, params);

        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CanisterBlockEntity be))
            return lootDrops;

        FluidStack contents = be.tank.getCapability().getFluidInTank(0);

        DataComponentPatch components = be.getComponentPatch()
                .forget(c -> c.equals(CDGDataComponents.FLUID_CONTENTS));
        if (components.isEmpty() && contents.isEmpty())
            return lootDrops;

        return lootDrops.stream()
                .peek(stack -> {
                    if (stack.getItem() instanceof CanisterBlockItem) {
                        stack.applyComponents(components);
                        stack.set(CDGDataComponents.FLUID_CONTENTS, SimpleFluidContent.copyOf(contents));
                    }
                })
                .toList();
    }

    @Override
    public Class<CanisterBlockEntity> getBlockEntityClass() {
        return CanisterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CanisterBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.CANISTER.get();
    }
}
