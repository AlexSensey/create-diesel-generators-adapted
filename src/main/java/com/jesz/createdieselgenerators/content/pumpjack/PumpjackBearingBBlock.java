package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;

public class PumpjackBearingBBlock extends Block implements IWrenchable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public PumpjackBearingBBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return CDGBlocks.PUMPJACK_BEARING.getDefaultState().setValue(BearingBlock.FACING, originalState.getValue(FACING));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        return CDGBlocks.PUMPJACK_BEARING.asStack();
    }

    @Override
    public Item asItem() {
        return CDGBlocks.PUMPJACK_BEARING.asStack().getItem();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
