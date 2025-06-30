package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGSpriteShifts;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;

public class ModularDieselEngineCTBehavior extends ConnectedTextureBehaviour {
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
        return CDGSpriteShifts.MODULAR_DIESEL_ENGINE;
    }

    @Override
    public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.CROSS;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter level, BlockPos pos, BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        if (!(state.getBlock() instanceof ModularDieselEngineBlock && other.getBlock() instanceof ModularDieselEngineBlock))
            return false;
        return ConnectivityHandler.isConnected(level, pos, otherPos);
    }
}
