package com.jesz.createdieselgenerators.content.bulk_fermenter;

import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IMultiBlockEntityContainerFluidItem extends IMultiBlockEntityContainer {
    default boolean hasInventory() { return false; }

    default boolean hasTank() { return false; }

    default int getTankSize(int tank) {	return 0; }

    default void setTankSize(int tank, int blocks) {}

    default IFluidTank getTank(int tank) { return null; }

    default FluidStack getFluid(int tank) {	return FluidStack.EMPTY; }
}
