package com.jesz.createdieselgenerators.content.diesel_engine.normal;

import com.jesz.createdieselgenerators.CDGSounds;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;
import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.StressImpact;

public class DieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine {
    public SmartFluidTankBehaviour tank;
    public AbstractComputerBehaviour computerBehaviour;
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    int tick;
    EngineUpgrades upgrade = EngineUpgrades.NONE;

    BlockState state;
    public DieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.state = state;
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (computerBehaviour.isPeripheralCap(cap))
            return computerBehaviour.getPeripheralCapability();
        if(state.getValue(FACING) == Direction.DOWN) {
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.WEST)
                return tank.getCapability().cast();
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.EAST)
                return tank.getCapability().cast();
        }else if(state.getValue(FACING) == Direction.UP){
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.NORTH)
                return tank.getCapability().cast();
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.SOUTH)
                return tank.getCapability().cast();
        }else{
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.DOWN)
                return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if(cap == ForgeCapabilities.FLUID_HANDLER)
            return tank.getCapability().cast();
        return super.getCapability(cap);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("Tick", tick);
        if (clientPacket)
            compound.putFloat("Pitch", targetStressPitch);
        compound.putString("Upgrade", upgrade.getId().toString());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tick = compound.getInt("Tick");
        if (clientPacket)
            targetStressPitch = compound.getFloat("Pitch");

        upgrade = EngineUpgrades.NONE;
        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades){
            if(upgrade.getId().toString().equals(compound.getString("Upgrade"))){
                this.upgrade = upgrade;
                break;
            }
        }
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));

        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new DieselEngineValueBox());
        movementDirection.withCallback($ -> onDirectionChanged());

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }
    public void onDirectionChanged(){}
    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }
    @Override
    public float calculateAddedStressCapacity() {
        if(getGeneratedSpeed() == 0 || !enabled())
            return 0;
        float capacity = upgrade.getStress(getFuelStress(), this);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if(!enabled())
            return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1) * upgrade.getStress(IEngine.super.getFuelSpeed(), this), state.getValue(FACING));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!StressImpact.isEnabled())
            return added;

        float stressBase = calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
    }
    float currentStressPitch = 0;
    float targetStressPitch = 0;
    @Override
    public void tick() {
        super.tick();
        state = getBlockState();
        reActivateSource = true;
        currentStressPitch = Mth.lerp(0.2f, currentStressPitch, targetStressPitch);
        if (level.isClientSide && enabled())
            upgrade.playSounds(tick, this);
        if(enabled()){
            tickFuelUsage();
            tick++;
        }

    }

    @Override
    public void playSound() {
        if (level.isClientSide && Minecraft.getInstance().player.position().distanceTo(Vec3.atBottomCenterOf(worldPosition)) < 15)
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), CDGSounds.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, 0.3f,1f + currentStressPitch, false);
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        if(maxStress == 0)
            targetStressPitch = 0;
        else
            targetStressPitch = currentStress / maxStress * 4;
        sendData();
    }

    @Override
    public int getTick() {
        return tick;
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public SmartFluidTankBehaviour getTank() {
        return tank;
    }
}
