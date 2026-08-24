package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.recipe.RecipeFinder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class BasinSpoutCasting implements BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        BasinBlockEntity basin;
        if (level.getBlockEntity(pos) instanceof BasinBlockEntity be)
            basin = be;
        else
            return 0;

        List<CastingRecipe> recipes = RecipeFinder.get(null, level,
                        holder -> holder.value().getType() == CDGRecipes.CASTING.getType()).stream()
                .map(holder -> holder.value())
                .filter(CastingRecipe.class::isInstance)
                .map(CastingRecipe.class::cast)
                .filter(cr -> cr.matches(basin, availableFluid)
                        && cr.getFluidIngredients().get(0).amount() <= availableFluid.getAmount())
                .toList();

        if (recipes.isEmpty())
            return 0;

        CastingRecipe recipe = recipes.get(0);
        return recipe.execute(basin, simulate);
    }
}
