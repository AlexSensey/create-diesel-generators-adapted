package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DieselEngineMovementBehaviour implements MovementBehaviour {

    @Override
    public boolean isActive(MovementContext context) {
        return context.contraption instanceof CarriageContraption && MovementBehaviour.super.isActive(context);
    }

    @Nullable
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return CDGBlocks.DIESEL_ENGINE.asStack();
    }

    @Override
    public void tick(MovementContext context) {
        if (!context.world.isClientSide())
            return;

        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> ClientDieselEngineMovementSounds.tick(context));
    }
}
