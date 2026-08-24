package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import java.util.HashMap;
import java.util.Map;

public class BulkFermenterBlock extends Block implements IBE<BulkFermenterBlockEntity>, IWrenchable {
    private static final Map<RemovedFermenterKey, BulkFermenterBlockEntity> REMOVED_FERMENTERS = new HashMap<>();
    public BulkFermenterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (direction == Direction.DOWN && neighbourState.getBlock() != this)
            withBlockEntityDo(level, pos, BulkFermenterBlockEntity::updateHeat);
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        withBlockEntityDo(world, pos, BulkFermenterBlockEntity::updateConnectivity);
        withBlockEntityDo(world, pos, BulkFermenterBlockEntity::updateHeat);
    }
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
        BulkFermenterBlockEntity removed = REMOVED_FERMENTERS.remove(new RemovedFermenterKey(world.dimension(), pos));
        if (removed != null)
            ConnectivityHandler.splitMultiAndReconnect(removed);
        super.affectNeighborsAfterRemoval(state, world, pos, isMoving);
    }

    static void prepareRemoval(BulkFermenterBlockEntity fermenter) {
        if (fermenter.hasLevel())
            REMOVED_FERMENTERS.put(new RemovedFermenterKey(fermenter.getLevel().dimension(), fermenter.getBlockPos()), fermenter);
    }

    private record RemovedFermenterKey(ResourceKey<Level> dimension, BlockPos pos) {}
    @Override
    public Class<BulkFermenterBlockEntity> getBlockEntityClass() {
        return BulkFermenterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BulkFermenterBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.BULK_FERMENTER.get();
    }

    // Tanks are less noisy when placed in batch
    public static final SoundType SILENCED_METAL =
            new DeferredSoundType(0.1F, 1.5F, () -> SoundEvents.METAL_BREAK, () -> SoundEvents.METAL_STEP,
                    () -> SoundEvents.METAL_PLACE, () -> SoundEvents.METAL_HIT, () -> SoundEvents.METAL_FALL);

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        SoundType soundType = super.getSoundType(state, world, pos, entity);
        if (entity != null && entity.getPersistentData()
                .contains("SilenceTankSound"))
            return SILENCED_METAL;
        return soundType;
    }
}
