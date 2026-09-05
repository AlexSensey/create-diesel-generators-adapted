package com.jesz.createdieselgenerators.content.andesite_girder;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.placement.IPlacementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AndesiteGirderWrenchBehaviour {
    @Nullable
    static Pair<Direction, Action> getDirectionAndAction(BlockHitResult result, Level world, BlockPos pos) {
        List<Pair<Direction, Action>> validDirections = getValidDirections(world, pos);

        if (validDirections.isEmpty())
            return null;

        List<Direction> directions = IPlacementHelper.orderedByDistance(pos, result.getLocation(),
                validDirections.stream()
                        .map(Pair::getFirst)
                        .toList());

        if (directions.isEmpty())
            return null;

        Direction dir = directions.get(0);
        return validDirections.stream()
                .filter(pair -> pair.getFirst() == dir)
                .findFirst()
                .orElseGet(() -> Pair.of(dir, Action.SINGLE));
    }

    public static List<Pair<Direction, Action>> getValidDirections(BlockGetter level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);

        if (!CDGBlocks.ANDESITE_GIRDER.has(blockState))
            return Collections.emptyList();

        return Arrays.stream(Iterate.directions)
                .<Pair<Direction, Action>>mapMulti((direction, consumer) -> {
                    BlockState other = level.getBlockState(pos.relative(direction));

                    if (!blockState.getValue(GirderBlock.X) && !blockState.getValue(GirderBlock.Z))
                        return;

                    // up and down
                    if (direction.getAxis() == Direction.Axis.Y) {
                        // no other girder in target dir
                        if (!CDGBlocks.ANDESITE_GIRDER.has(other)) {
                            if (!blockState.getValue(GirderBlock.X) ^ !blockState.getValue(GirderBlock.Z))
                                consumer.accept(Pair.of(direction, Action.SINGLE));
                            return;
                        }
                        // this girder is a pole or cross
                        if (blockState.getValue(GirderBlock.X) == blockState.getValue(GirderBlock.Z))
                            return;
                        // other girder is a pole or cross
                        if (other.getValue(GirderBlock.X) == other.getValue(GirderBlock.Z))
                            return;
                        // toggle up/down connection for both
                        consumer.accept(Pair.of(direction, Action.PAIR));

                        return;
                    }

//					if (BlockRegistry.ANDESITE_GIRDER.has(other))
//						consumer.accept(Pair.of(direction, Action.HORIZONTAL));

                })
                .toList();
    }

    public static boolean handleClick(Level level, BlockPos pos, BlockState state, BlockHitResult result) {
        Pair<Direction, Action> dirPair = getDirectionAndAction(result, level, pos);
        if (dirPair == null)
            return false;
        if (level.isClientSide())
            return true;
        if (!state.getValue(GirderBlock.X) && !state.getValue(GirderBlock.Z))
            return false;

        Direction dir = dirPair.getFirst();

        BlockPos otherPos = pos.relative(dir);
        BlockState other = level.getBlockState(otherPos);

        if (dir == Direction.UP) {
            level.setBlock(pos, postProcess(state.cycle(GirderBlock.TOP)), 2 | 16);
            if (dirPair.getSecond() == Action.PAIR && CDGBlocks.ANDESITE_GIRDER.has(other))
                level.setBlock(otherPos, postProcess(other.cycle(GirderBlock.BOTTOM)), 2 | 16);
            return true;
        }

        if (dir == Direction.DOWN) {
            level.setBlock(pos, postProcess(state.cycle(GirderBlock.BOTTOM)), 2 | 16);
            if (dirPair.getSecond() == Action.PAIR && CDGBlocks.ANDESITE_GIRDER.has(other))
                level.setBlock(otherPos, postProcess(other.cycle(GirderBlock.TOP)), 2 | 16);
            return true;
        }

//		if (dirPair.getSecond() == Action.HORIZONTAL) {
//			BooleanProperty property = dir.getAxis() == Direction.Axis.X ? GirderBlock.X : GirderBlock.Z;
//			level.setBlock(pos, state.cycle(property), 2 | 16);
//
//			return true;
//		}

        return true;
    }

    private static BlockState postProcess(BlockState newState) {
        if (newState.getValue(GirderBlock.TOP) && newState.getValue(GirderBlock.BOTTOM))
            return newState;
        if (newState.getValue(GirderBlock.AXIS) != Direction.Axis.Y)
            return newState;
        return newState.setValue(GirderBlock.AXIS, newState.getValue(GirderBlock.X) ? Direction.Axis.X : Direction.Axis.Z);
    }

    enum Action {
        SINGLE, PAIR, HORIZONTAL
    }
    
}
