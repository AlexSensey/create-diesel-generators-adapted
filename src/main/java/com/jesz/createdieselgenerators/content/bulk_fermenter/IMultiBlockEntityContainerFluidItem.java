package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IMultiBlockEntityContainerFluidItem extends IMultiBlockEntityContainer.Fluid {
    default boolean hasInventory() { return false; }
}
