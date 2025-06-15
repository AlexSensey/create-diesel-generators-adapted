package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

public class PumpjackHoleGenerator {
    public static void blockState(DataGenContext<Block, PumpjackHoleBlock> c, RegistrateBlockstateProvider p) {
        MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "open"))
                .rotationY(180)
                .addModel()
                .condition(BlockStateProperties.NORTH, true)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "open"))
                .rotationY(270)
                .addModel()
                .condition(BlockStateProperties.EAST, true)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "open"))
                .rotationY(90)
                .addModel()
                .condition(BlockStateProperties.WEST, true)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "open"))
                .addModel()
                .condition(BlockStateProperties.SOUTH, true)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "flat"))
                .rotationY(180)
                .addModel()
                .condition(BlockStateProperties.NORTH, false)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "flat"))
                .rotationY(270)
                .addModel()
                .condition(BlockStateProperties.EAST, false)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "flat"))
                .rotationY(90)
                .addModel()
                .condition(BlockStateProperties.WEST, false)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p, "flat"))
                .addModel()
                .condition(BlockStateProperties.SOUTH, false)
                .end();

        builder.part()
                .modelFile(AssetLookup.partialBaseModel(c, p))
                .addModel()
                .end();
    }
}
