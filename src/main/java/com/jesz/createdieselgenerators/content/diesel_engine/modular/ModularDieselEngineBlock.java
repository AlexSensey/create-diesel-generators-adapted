package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.ICDGKinetics;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.placement.PoleHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.POWERED;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class ModularDieselEngineBlock extends HorizontalKineticBlock implements IBE<ModularDieselEngineBlockEntity>, SpecialBlockItemRequirement, ICDGKinetics {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public ModularDieselEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState()
                    .setValue(PIPE, true)
                    .setValue(POWERED, false));
    }
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        withBlockEntityDo(level, pos, ModularDieselEngineBlockEntity::updateConnectivity);
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        ModularDieselEngineBlockEntity be = level.getBlockEntity(pos, CDGBlockEntityTypes.LARGE_DIESEL_ENGINE.get()).orElse(null);
        if (be == null) {
            super.neighborChanged(state, level, pos, block, otherPos, moving);
            return;
        }
        ModularDieselEngineBlockEntity controller = be.controller;
        if (controller == null)
            controller = be;

        boolean powered = false;

        if (level.hasNeighborSignal(pos))
            powered = true;
        else
            for (int i = 0; i < controller.length; i++)
                if (level.hasNeighborSignal(controller.getBlockPos().relative(controller.getBlockState().getValue(FACING).getAxis(), -i)))
                    powered = true;

        level.setBlock(controller.getBlockPos(), controller.getBlockState().setValue(POWERED, powered), 2);

        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(itemInHand)) {
                placementHelper.getOffset(player, level, state, pos, hit)
                        .placeInWorld(level, (BlockItem) itemInHand.getItem(), player, hand, hit);
                return InteractionResult.SUCCESS;
            }
        }

        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades) {
            if (upgrade == EngineUpgrades.NONE)
                continue;
            if (upgrade.getItem().is(itemInHand.getItem())) {
                withBlockEntityDo(level, pos, be -> {
                    if (!upgrade.canAddOn(be))
                        return;
                    if(be.upgrade != EngineUpgrades.NONE || (be.controller != null && be.controller.upgrade != EngineUpgrades.NONE))
                        return;

                    if(!player.isCreative())
                        itemInHand.shrink(1);
                    be.upgrade = upgrade;
                    IWrenchable.playRotateSound(level, pos);
                });
                return InteractionResult.SUCCESS;
            }
        }
        if(!CDGConfig.ENGINES_FILLED_WITH_ITEMS.get())
            return super.use(state, level, pos, player, hand, hit);
        if (itemInHand.isEmpty())
            return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof SmartBlockEntity be){
            IFluidHandler tank = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if(tank == null)
                return InteractionResult.PASS;
            if(itemInHand.getItem() instanceof BucketItem bi) {
                if (!tank.getFluidInTank(0).isEmpty())
                    return InteractionResult.FAIL;
                tank.fill(new FluidStack(bi.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative())
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            if(itemInHand.getItem() instanceof MilkBucketItem) {
                if (!tank.getFluidInTank(0).isEmpty())
                    return InteractionResult.FAIL;
                tank.fill(new FluidStack(ForgeMod.MILK.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative())
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            IFluidHandlerItem itemTank = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if(itemTank == null)
                return InteractionResult.PASS;
            itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace() == Direction.UP){
            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), updateAfterWrenched(state.setValue(PIPE, !state.getValue(PIPE)), context));
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
            ModularDieselEngineBlockEntity controller = be.controller;
            if(be.upgrade != EngineUpgrades.NONE || (controller != null && controller.upgrade != EngineUpgrades.NONE)){
                if(!context.getLevel().isClientSide) {
                    if (controller != null) {
                        if (!context.getPlayer().isCreative())
                            context.getPlayer().getInventory().placeItemBackInInventory(controller.upgrade.getItem());
                        controller.upgrade = EngineUpgrades.NONE;
                        IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
                        return;
                    }
                    if (!context.getPlayer().isCreative())
                        context.getPlayer().getInventory().placeItemBackInInventory(be.upgrade.getItem());
                    be.upgrade = EngineUpgrades.NONE;
                    IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
                }
            }
        });
        return InteractionResult.SUCCESS;
    }
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(PIPE, POWERED);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(pContext.getPlayer().isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        else
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        withBlockEntityDo(level, pos, ModularDieselEngineBlockEntity::removed);
        if (!state.is(newState.getBlock()))
            withBlockEntityDo(level, pos, be -> {
                if (be.upgrade != EngineUpgrades.NONE)
                    popResource(level, pos, be.upgrade.getItem());
            });
        super.onRemove(state, level, pos, newState, isMoving);
    }
    @Override
    public Class<ModularDieselEngineBlockEntity> getBlockEntityClass() {
        return ModularDieselEngineBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends ModularDieselEngineBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.LARGE_DIESEL_ENGINE.get();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pState.getValue(FACING) == NORTH || pState.getValue(FACING) == SOUTH){
            return Shapes.or(Shapes.block(), Block.box(-2,0,0,18,4,16));
        }else{
            return Shapes.or(Shapes.block(), Block.box(0,0,-2,16,4,18));
        }
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING)
                .getAxis() == face.getAxis();
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING)
                .getAxis();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        List<ItemStack> list = new ArrayList<>();
        list.add(CDGBlocks.MODULAR_DIESEL_ENGINE.asStack());
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
    }

    @Override
    public float getDefaultStressCapacity() {
        return 2048;
    }

    @Override
    public float getDefaultStressStressImpact() {
        return 0;
    }

    @Override
    public float getDefaultSpeed() {
        return 96;
    }

    private static class PlacementHelper extends PoleHelper<Direction>{

        public PlacementHelper() {
            super(CDGBlocks.MODULAR_DIESEL_ENGINE::has, state -> state.getValue(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CDGBlocks.MODULAR_DIESEL_ENGINE::isIn;
        }
    }
}
