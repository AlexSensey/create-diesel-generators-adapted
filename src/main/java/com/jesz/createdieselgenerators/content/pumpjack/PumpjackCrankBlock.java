package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Predicate;

public class PumpjackCrankBlock extends HorizontalKineticBlock implements IBE<PumpjackCrankBlockEntity> {
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public PumpjackCrankBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if (state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X)
            return Shapes.or(Block.box(0, 0, 0, 16, 16, 16), Block.box(4, 16, 0, 12, 22, 16));
        else
            return Shapes.or(Block.box(0, 0, 0, 16, 16, 16), Block.box(0, 16, 4, 16, 22, 12));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING)
                .getAxis() == face.getAxis();
    }

    @Override
    public Class<PumpjackCrankBlockEntity> getBlockEntityClass() {
        return PumpjackCrankBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpjackCrankBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.PUMPJACK_CRANK.get();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult)
                        .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static class PlacementHelper implements IPlacementHelper{

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CDGBlocks.PUMPJACK_BEARING::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return b -> true;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {

            if(state.getBlock() instanceof PumpjackCrankBlock) {
                boolean isLarge = world.getBlockEntity(pos) instanceof PumpjackCrankBlockEntity crankBE && crankBE.crankSize.getValue() == 1;
                if(world.getBlockState(pos.above(isLarge ? 4 : 3)).getBlock() instanceof AirBlock)
                    return PlacementOffset.success(pos.above(isLarge ? 4 : 3))
                            .withTransform((b) -> CDGBlocks.PUMPJACK_BEARING_B.getDefaultState().setValue(PumpjackBearingBBlock.FACING, state.getValue(HORIZONTAL_FACING)))
                            .withGhostState(CDGBlocks.PUMPJACK_BEARING_B.getDefaultState().setValue(PumpjackBearingBBlock.FACING, state.getValue(HORIZONTAL_FACING)));
            }
            return PlacementOffset.fail();
        }
    }
}
