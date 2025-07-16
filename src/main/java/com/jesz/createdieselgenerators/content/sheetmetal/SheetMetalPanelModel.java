package com.jesz.createdieselgenerators.content.sheetmetal;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock.FACING;
import static com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock.ROLL;

public class SheetMetalPanelModel extends BakedModelWrapperWithData {
    protected static final ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

    public SheetMetalPanelModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    protected ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter level, BlockPos pos, BlockState state,
                                                ModelData blockEntityData) {
        CullData cullData = new CullData();

        for (Direction d : Iterate.directions){
            if(d.getAxis() == state.getValue(FACING).getAxis())
                continue;
            BlockState otherState = level.getBlockState(pos.relative(d));
            if(!CDGBlocks.SHEET_METAL_PANEL.has(otherState))
                continue;
            if(otherState.getValue(FACING) != state.getValue(FACING))
                continue;
            if(otherState.getValue(ROLL) != state.getValue(ROLL))
                continue;
            if(!state.getValue(ROLL) && state.getValue(FACING).getAxis().isHorizontal() && d.getAxis().isHorizontal())
                continue;
            if(state.getValue(ROLL) && state.getValue(FACING).getAxis().isHorizontal() && d.getAxis().isVertical())
                continue;
            if(state.getValue(FACING).getAxis().isVertical() && (d.getAxis() == Direction.Axis.Z && state.getValue(ROLL) || (d.getAxis() == Direction.Axis.X && !state.getValue(ROLL))))
                    continue;
            cullData.setCulled(d, true);
        }
        return builder.with(CULL_PROPERTY, cullData);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, RenderType renderType) {

        List<BakedQuad> quads = new ArrayList<>();

        quads.addAll(super.getQuads(state, side, rand, extraData, renderType));
        quads.removeIf(q -> extraData.has(CULL_PROPERTY) && extraData.get(CULL_PROPERTY) != null && extraData.get(CULL_PROPERTY)
                .isCulled(q.getDirection()));
//        quads.addAll(super.getQuads(state, null, rand, extraData, renderType));
        return quads;
    }

    static class CullData {

        boolean[] culledFaces;
        public CullData() {
            culledFaces = new boolean[3];
            Arrays.fill(culledFaces, false);
        }

        void setCulled(Direction face, boolean cull) {
            culledFaces[face.getAxis().ordinal()] = cull;
        }

        boolean isCulled(Direction face) {
            return culledFaces[face.getAxis().ordinal()];
        }
    }

}
