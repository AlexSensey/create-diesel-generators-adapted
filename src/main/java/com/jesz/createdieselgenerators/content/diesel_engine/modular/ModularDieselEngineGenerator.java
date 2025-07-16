package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public class ModularDieselEngineGenerator extends SpecialBlockStateGen {

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        Direction direction = state.getValue(ModularDieselEngineBlock.FACING);
        return (int) (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? direction.toYRot() : direction.getOpposite().toYRot());
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {
        return state.getValue(ModularDieselEngineBlock.PIPE) ? prov.models().getExistingFile(prov.modLoc("block/modular_diesel_engine/block_pipe"))
                : prov.models().getExistingFile(prov.modLoc("block/modular_diesel_engine/block"));
    }
}
