package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

public class BulkFermentingRecipe extends ProcessingRecipe<SmartInventory> {
    public BulkFermentingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params){
        super(CDGRecipes.BULK_FERMENTING, params);
    }
    @Override
    protected int getMaxInputCount() {
        return 9;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 2;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(SmartInventory inventory, Level level) {
        return false;
    }

    public boolean test(IItemHandler container){
        if(container == null)
            return false;
        for (Ingredient ingredient : getIngredients()) {
            boolean valid = false;
            for (int i = 0; i < container.getSlots(); i++) {
                ItemStack stack = container.getStackInSlot(i);
                if (!ingredient.test(stack))
                    continue;
                ItemStack[] items = ingredient.getItems();
                if (items.length == 0 || items[0].getCount() > stack.getCount())
                    continue;
                valid = true;
            }
            if (!valid)
                return false;
        }
        return true;
    }
    public boolean test(BulkFermenterBlockEntity.BulkFermenterFluidHandler container){
        if(container == null)
            return false;
        for(FluidIngredient ingredient : getFluidIngredients()){
            boolean valid = false;
            for (int i = 0; i < container.getTanks(); i++) {
                FluidStack fluidInTank = container.getFluidInTank(i);
                if (ingredient.test(fluidInTank) && ingredient.getRequiredAmount() <= fluidInTank.getAmount()) {
                    valid = true;
                    break;
                }
            }
            if(!valid)
                return false;
        }
        return true;
    }

    public void remove(BulkFermenterBlockEntity.BulkFermenterFluidHandler container){
        if (container == null)
            return;
        for (FluidIngredient ingredient : getFluidIngredients()){
            for (int i = 0; i < container.getTanks(); i++) {
                FluidStack fluidInTank = container.getFluidInTank(i);
                if (ingredient.test(fluidInTank) && ingredient.getRequiredAmount() <= fluidInTank.getAmount()) {
                    FluidStack toDrain = fluidInTank.copy();
                    toDrain.setAmount(ingredient.getRequiredAmount());
                    container.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
                    break;
                }
            }
        }
    }
    public void remove(IItemHandler container){
        if(container == null)
            return;
        for (Ingredient ingredient : getIngredients()){
            for (int i = 0; i < container.getSlots(); i++) {
                ItemStack stack = container.getStackInSlot(i);
                if(!ingredient.test(stack))
                    continue;
                ItemStack[] items = ingredient.getItems();
                if(items.length == 0 || items[0].getCount() > stack.getCount())
                    continue;
                container.extractItem(i, items[0].getCount(), false);
                break;
            }
        }
    }
}
