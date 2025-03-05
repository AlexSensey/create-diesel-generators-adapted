package com.jesz.createdieselgenerators.content.andesite_girder;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class AndesiteGirderEncasedShaftBlock extends GirderEncasedShaftBlock {
    public AndesiteGirderEncasedShaftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return CDGBlocks.ANDESITE_GIRDER.getDefaultState()
                .setValue(WATERLOGGED, originalState.getValue(WATERLOGGED))
                .setValue(GirderBlock.X, originalState.getValue(HORIZONTAL_AXIS) == Direction.Axis.Z)
                .setValue(GirderBlock.Z, originalState.getValue(HORIZONTAL_AXIS) == Direction.Axis.X)
                .setValue(GirderBlock.AXIS, originalState.getValue(HORIZONTAL_AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X)
                .setValue(GirderBlock.BOTTOM, originalState.getValue(BOTTOM))
                .setValue(GirderBlock.TOP, originalState.getValue(TOP));
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), be)
                .union(ItemRequirement.of(AllBlocks.METAL_GIRDER.getDefaultState(), be));
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.ENCASED_GIRDER.get();
    }
}
