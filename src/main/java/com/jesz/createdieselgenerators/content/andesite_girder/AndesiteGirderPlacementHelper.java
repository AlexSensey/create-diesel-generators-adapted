package com.jesz.createdieselgenerators.content.andesite_girder;

import com.google.common.base.Predicates;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.content.decoration.girder.GirderPlacementHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class AndesiteGirderPlacementHelper extends GirderPlacementHelper {
    @Override
    public Predicate<BlockState> getStatePredicate() {
        return Predicates.or(CDGBlocks.ANDESITE_GIRDER::has, CDGBlocks.ANDESITE_GIRDER_ENCASED_SHAFT::has);
    }

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return CDGBlocks.ANDESITE_GIRDER::isIn;
    }
}
