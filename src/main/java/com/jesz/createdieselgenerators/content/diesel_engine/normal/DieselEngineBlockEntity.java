package com.jesz.createdieselgenerators.content.diesel_engine.normal;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public class DieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine {
    ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;

    EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    SmartFluidTankBehaviour tank;
    private float lastCapacity;
    private float lastSpeed;
    private int analogSignal = 0;
    private boolean signalChanged = false;
    private float fuelDebt = 0f;
    private FuelType cachedFuelType = FuelType.EMPTY;
    private FluidStack lastCachedFluid = FluidStack.EMPTY;
    private float cachedFuelSpeed = 0f;
    private float cachedFuelCapacity = 0f;
    private float cachedBurnRate = 0f;

    public DieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK,
                CDGBlockEntityTypes.DIESEL_ENGINE.get(),
                (be, side) -> {
                    if (side == null)
                        return com.jesz.createdieselgenerators.foundation.FluidCompatibility.resourceHandler(be.tank.getCapability());
                    Direction facing = be.getBlockState().getValue(FACING);
                    if (facing.getAxis().isVertical()) {
                        Direction.Axis portAxis = (facing == Direction.DOWN)
                                ? Direction.Axis.X
                                : Direction.Axis.Z;
                        if (side.getAxis() == portAxis)
                            return com.jesz.createdieselgenerators.foundation.FluidCompatibility.resourceHandler(be.tank.getCapability());
                    } else {
                        if (side == Direction.DOWN)
                            return com.jesz.createdieselgenerators.foundation.FluidCompatibility.resourceHandler(be.tank.getCapability());
                    }
                    return null;
                });
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putString("Upgrade", upgrade.getId().toString());
        tag.putInt("AnalogSignal", analogSignal);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        upgrade = EngineUpgrades.get(Identifier.parse(tag.getStringOr("Upgrade", "")));
        analogSignal = tag.getIntOr("AnalogSignal", 0);
        fuelDebt = 0f;
        invalidateFuelCache();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new DieselEngineValueBox());
        movementDirection.withCallback(v -> reActivateSource = true);
        tank = SmartFluidTankBehaviour.single(this, 1000);

        behaviours.add(movementDirection);
        behaviours.add(tank);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float baseFuelSpeed = getFuelSpeed();
        float speed = upgrade.getSpeed(baseFuelSpeed, this) * getThrottle();
        float capacity = upgrade.getCapacity(getFuelCapacity() * baseFuelSpeed, this) / Math.max(0.01f, speed);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!enabled()) return 0;
        float throttle = getThrottle();
        if (throttle == 0f) return 0;
        return convertToDirection(
                (movementDirection.getValue() == 1 ? -1 : 1)
                        * upgrade.getSpeed(getFuelSpeed(), this)
                        * throttle,
                getBlockState().getValue(FACING));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getGeneratedSpeed() != 0)
            super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (signalChanged) {
            signalChanged = false;
            reActivateSource = true;
            setChanged();
            sendData();
        }

        if (!level.isClientSide()) {
            float currentCapacity = 0;
            float currentSpeed = 0;

            if (validFS()) {
                float throttle = getThrottle();

                if (throttle != 0f || lastSpeed != 0f) {
                    float baseFuelSpeed = getFuelSpeed();
                    float baseSpeed = upgrade.getSpeed(getFuelSpeed(), this) * throttle;

                    currentCapacity = upgrade.getCapacity(getFuelCapacity() * baseFuelSpeed, this) / Math.max(0.01f, baseSpeed);
                    currentSpeed = getGeneratedSpeed();
                }
            }

            if (lastSpeed != currentSpeed || lastCapacity != currentCapacity) {
                reActivateSource = true;
                lastSpeed = currentSpeed;
                lastCapacity = currentCapacity;
            }
        }

        if (enabled() && !isOverStressed()) {
            fuelDebt += cachedBurnRate * getFuelThrottle();
            while (fuelDebt >= 1f) {
                tank.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
                fuelDebt -= 1f;
            }
        }

        // Sound lifetime must follow the block entity tick, not its renderer.
        // Render submission is culled when the player looks away, which used to
        // stop keepAlive() calls and incorrectly fade a still-running engine.
        if (level.isClientSide())
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () ->
                    com.jesz.createdieselgenerators.content.diesel_engine.ClientEngineSounds.tick(
                            this, Vec3.atCenterOf(getBlockPos()), 1, isOverStressed()));

    }

    public void setAnalogSignal(int newSignal) { analogSignal = newSignal; }
    public void setSignalChanged(boolean newSignal) { signalChanged = newSignal; }

    @Override
    public int getAnalogSignal() {
        return analogSignal;
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public FluidTank getTank() {
        return tank.getPrimaryHandler();
    }

    @Override
    public EngineUpgrades getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(EngineUpgrades upgrade) {
        this.upgrade = upgrade;
    }

    @Override public FuelType getCachedFuelType() { return cachedFuelType; }
    @Override public void setCachedFuelType(FuelType t) { cachedFuelType = t; }
    @Override public FluidStack getLastCachedFluid() { return lastCachedFluid; }
    @Override public void setLastCachedFluid(FluidStack f) { lastCachedFluid = f; }
    @Override public float getCachedFuelSpeed() { return cachedFuelSpeed; }
    @Override public void setCachedFuelSpeed(float s) { cachedFuelSpeed = s; }
    @Override public float getCachedFuelCapacity() { return cachedFuelCapacity; }
    @Override public void setCachedFuelCapacity(float c) { cachedFuelCapacity = c; }
    @Override public float getCachedBurnRate() { return cachedBurnRate; }
    @Override public void setCachedBurnRate(float r) { cachedBurnRate = r; }
}
