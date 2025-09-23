package com.jesz.createdieselgenerators.content.burner;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class BurnerBlockEntity extends KineticBlockEntity {
    public float heat = -1;
    SmartFluidTank tank = new SmartFluidTank(100, f -> {});

    public float valveState = 0.2f;
    public float prevValveState;
    int tick;
    float multiplier;
    boolean ignited = false;
    int ignitionTries;
    @Override
    public void tick() {
        tick = (tick+1) % 40000;
        super.tick();

        prevValveState = valveState;
        valveState = Mth.clamp(valveState + getSpeed() / 5000, 0, 1);

        boolean containsValidFuel = !tank.getFluid().isEmpty();
        if (containsValidFuel)
            multiplier = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), tank.getFluid().getFluid()).burnerStrength();
        if (multiplier == 0)
            containsValidFuel = false;

        if (valveState == 0 || !containsValidFuel) {
            heat = -1;
            if(!level.isClientSide) {
                if (ignited)
                    level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, level.getRandom().nextFloat() * 0.4F + 0.7F);

                ignited = false;
                ignitionTries = 0;
            }
        }
        if (containsValidFuel && valveState != 0){
            heat = (valveState + 1) * multiplier;
            if(level.isClientSide)
                return;
            if(tick % 5 == 0){
                if(!ignited){
                    level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.5f, level.getRandom().nextFloat() * 0.4F + 0.7F);
                    ignitionTries++;
                    if(ignitionTries > 4)
                        ignited = true;
                }
            }
            if ((int)(tick % (10 / valveState)) == 0) {
                tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
                sendData();
                setChanged();
            }
        }
        if (level.isClientSide)
            return;
        if (getBlockState().getValue(BurnerBlock.HEAT_LEVEL) != calculateHeatLevel(heat)) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BurnerBlock.HEAT_LEVEL, calculateHeatLevel(heat)).setValue(BurnerBlock.LIT, heat > 0));
            notifyUpdate();
        }

    }
    int x = 0;
    int y = 0;

    @Override
    public void tickAudio() {
        super.tickAudio();
        if (valveState == 0 || heat == -1)
            return;
        RandomSource random = RandomSource.create();
        if (tick % 100 == 0)
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.3f, 1f, true);

        x = (x + 1) % 4;
        if (x == 3)
            y = (y + 1) % 4;

        if (!(x == 0 || x == 3 || y == 0 || y == 3))
            return;
        if (heat >= 1.8f && random.nextInt(0, (int) (1+heat*2)) != 1) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    worldPosition.getX() + 0.3475 + (double) x / 9, worldPosition.getY() + 0.75, worldPosition.getZ() + 0.3475 + (double) y / 9,
                    0, 0.02 * valveState, 0);
            return;
        }
        level.addParticle(ParticleTypes.FLAME,
                worldPosition.getX() + 0.3475 + (float) x / 9, worldPosition.getY() + 0.75, worldPosition.getZ() + 0.3475 + (float) y / 9,
                0, 0.02 * valveState, 0);

        level.addParticle(ParticleTypes.SMOKE,
            worldPosition.getX() + 0.3475 + (float) x / 9, worldPosition.getY() + 0.75, worldPosition.getZ() + 0.3475 + (float) y / 9,
            0, 0.02 * valveState, 0);


    }

    public BlazeBurnerBlock.HeatLevel calculateHeatLevel(float heat) {
        if(heat >= 1.8)
            return BlazeBurnerBlock.HeatLevel.SEETHING;
        if(heat >= 1.4)
            return BlazeBurnerBlock.HeatLevel.KINDLED;
        if(heat >= 1.2)
            return BlazeBurnerBlock.HeatLevel.FADING;
        if(heat >= 1)
            return BlazeBurnerBlock.HeatLevel.SMOULDERING;
        return BlazeBurnerBlock.HeatLevel.NONE;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putFloat("ValveState", valveState);
        compound.putFloat("Heat", heat);
        compound.putInt("Tick", tick);
        compound.put("FluidContent", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        valveState = compound.getFloat("ValveState");
        heat = compound.getFloat("Heat");
        tick = compound.getInt("Tick");
        tank.readFromNBT(registries, compound.getCompound("FluidContent"));
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CDGBlockEntityTypes.BURNER.get(),
                (be, context) -> {
                    if (context != Direction.UP)
                        return be.tank;
                    return null;
                }
        );
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank);
    }

    public BurnerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }
}
