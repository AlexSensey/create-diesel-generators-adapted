package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.ICDGKinetics;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChemicalTurretBlock extends KineticBlock implements IBE<ChemicalTurretBlockEntity>, ICogWheel, ICDGKinetics {
    public ChemicalTurretBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(box(0, 0, 0, 16, 6, 16), box(1, 6, 1, 15, 15, 15));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof ChemicalTurretBlockEntity be)
            be.redstoneSignal = level.getBestNeighborSignal(pos);
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if((!state.hasBlockEntity() || state.getBlock() == newState.getBlock()) && !isMoving){
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof ChemicalTurretBlockEntity be)
                if(be.lighterUpgrade)
                    Block.popResource(level, pos, CDGItems.LIGHTER.asStack());
        }
        IBE.onRemove(state, level, pos, newState);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof ChemicalTurretBlockEntity be) {
            if (player.getItemInHand(hand).isEmpty())
                if (be.controllingPlayer == player) {
                    be.removePlayer();
                    return InteractionResult.SUCCESS;
                } else if (be.controllingPlayer == null) {
                    be.setControllingPlayer(player);
                    return InteractionResult.SUCCESS;
                }
            if(player.getItemInHand(hand).is(CDGItems.LIGHTER.get())){
                if(!be.lighterUpgrade) {
                    be.lighterUpgrade = true;
                    if(!level.isClientSide)
                        be.notifyUpdate();
                    if(!player.isCreative())
                        player.getItemInHand(hand).shrink(1);
                    IWrenchable.playRotateSound(level, pos);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if(blockEntity instanceof ChemicalTurretBlockEntity be){
            if(be.lighterUpgrade) {
                be.lighterUpgrade = false;
                if(!context.getLevel().isClientSide)
                    be.notifyUpdate();
                if(!context.getPlayer().isCreative())
                    context.getPlayer().getInventory().placeItemBackInInventory(CDGItems.LIGHTER.asStack());
                IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        }
        return super.onWrenched(state, context);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean isMoving) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof ChemicalTurretBlockEntity be)
            be.redstoneSignal = level.getBestNeighborSignal(pos);
        super.neighborChanged(state, level, pos, block, otherPos, isMoving);
    }
    @Override
    public Class<ChemicalTurretBlockEntity> getBlockEntityClass() {
        return ChemicalTurretBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChemicalTurretBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.CHEMICAL_TURRET.get();
    }

    @Override
    public float getDefaultStressCapacity() {
        return 0;
    }

    @Override
    public float getDefaultStressStressImpact() {
        return 4;
    }

    @Override
    public float getDefaultSpeed() {
        return 0;
    }
}
