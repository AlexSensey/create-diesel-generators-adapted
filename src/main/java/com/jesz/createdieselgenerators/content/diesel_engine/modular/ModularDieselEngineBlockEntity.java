package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.FACING;
import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.PIPE;

public class ModularDieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine, IMultiBlockEntityContainer.Fluid {
    protected ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    protected int length = 1;
    @NotNull
    protected EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    protected IFluidHandler fluidCapability;
    protected FluidTank tankInventory = new SmartFluidTank(1000, f -> sendData());
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity = false;
    protected boolean updateCapability = false;
    private float lastCapacity;
    private float lastSpeed;
    public int analogSignal = 0;
    private float fuelDebt = 0f;
    private boolean signalChanged = false;
    private FuelType cachedFuelType = FuelType.EMPTY;
    private FluidStack lastCachedFluid = FluidStack.EMPTY;
    private float cachedFuelSpeed = 0f;
    private float cachedFuelCapacity = 0f;
    private float cachedBurnRate = 0f;
    private boolean queuedRemovalSplit;

    public ModularDieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!queuedRemovalSplit && hasLevel() && !level.isClientSide()) {
            queuedRemovalSplit = true;
            ModularDieselEngineBlock.prepareRemoval(this);
        }
        super.preRemoveSideEffects(pos, state);
    }

    public void resetConnectivity() {
        updateConnectivity = true;
        controller = null;
        length = 1;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new ModularDieselEngineValueBox());
        movementDirection.withCallback(this::onDirectionChanged);

        behaviours.add(movementDirection);
        super.addBehaviours(behaviours);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK,
                CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get(),
                (be, side) -> {
                    if (be.fluidCapability == null)
                        be.refreshCapability();
                    if (side == null || (side == Direction.UP && be.getBlockState().getValue(PIPE)))
                        return com.jesz.createdieselgenerators.foundation.FluidCompatibility.resourceHandler(be.fluidCapability);
                    return null;
                });
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!isController()) {
            ModularDieselEngineBlockEntity controller = getControllerBE();
            if (controller == null)
                return false;
            return controller.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }
        if (getGeneratedSpeed() != 0)
            super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return containedFluidTooltip(tooltip, isPlayerSneaking, fluidCapability);
    }

    public void onDirectionChanged(int v) {
        ModularDieselEngineBlockEntity controller = getControllerBE();
        if (controller != null) {
            controller.movementDirection.setValue(v);
            controller.reActivateSource = true;

            for (int i = 0; i < controller.getHeight(); i++) {
                if (level.getBlockEntity(controller.getBlockPos().relative(controller.getBlockState().getValue(FACING).getAxis(), i)) instanceof ModularDieselEngineBlockEntity be && be.movementDirection.getValue() != v)
                    be.movementDirection.setValue(v);
            }
        }
    }

    @Override
    public float calculateAddedStressCapacity() {
        float baseFuelSpeed = getFuelSpeed();
        float speed = upgrade.getSpeed(baseFuelSpeed, this) * getThrottle();
        float capacity = upgrade.getCapacity(getFuelCapacity() * getHeight() * baseFuelSpeed, this) / Math.max(0.01f, speed);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!enabled() || !isController()) return 0;
        float throttle = getThrottle();
        if (throttle == 0f) return 0;
        return convertToDirection(
                (movementDirection.getValue() == 1 ? -1 : 1)
                        * upgrade.getSpeed(getFuelSpeed(), this)
                        * throttle,
                getBlockState().getValue(ModularDieselEngineBlock.FACING));
    }

    @Override
    public void tick() {
        super.tick();

        if (updateCapability) {
            updateCapability = false;
            refreshCapability();
        }
        if (updateConnectivity)
            updateConnectivity();

        if (!isController()) {
            if (upgrade == EngineUpgrades.EMPTY)
                return;
            ModularDieselEngineBlockEntity controller = getControllerBE();

            if (controller != null && controller.upgrade == EngineUpgrades.EMPTY)
                controller.setUpgrade(upgrade);
            else
                Block.popResource(level, getBlockPos(), upgrade.getItem());
            setUpgrade(EngineUpgrades.EMPTY);

            return;
        }

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
                currentSpeed = getGeneratedSpeed();
                currentCapacity = upgrade.getCapacity(
                        getFuelCapacity() * getHeight() * (1 / Math.max(upgrade.getSpeed(getFuelSpeed(), this) * throttle, 0.001f))
                                * upgrade.getSpeed(getFuelSpeed(), this) * throttle, this);
            }

            if (lastSpeed != currentSpeed || lastCapacity != currentCapacity) {
                reActivateSource = true;
                lastSpeed = currentSpeed;
                lastCapacity = currentCapacity;
            }
        }

        if (level.isClientSide()) {
            Vec3 soundPos = Vec3.atCenterOf(getBlockPos());
            if (getBlockState().getValue(FACING).getAxis() == Direction.Axis.X)
                soundPos = soundPos.add((double) length / 2 - .5, 0, 0);
            else
                soundPos = soundPos.add(0, 0, (double) length / 2 - .5);
            Vec3 finalSoundPos = soundPos;
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () ->
                    com.jesz.createdieselgenerators.content.diesel_engine.ClientEngineSounds.tick(
                            this, finalSoundPos, 1, isOverStressed()));
        }

        if (isOverStressed())
            return;

        fuelDebt += (length * cachedBurnRate) * getFuelThrottle();
        while (fuelDebt >= 1f) {
            tankInventory.drain(1, IFluidHandler.FluidAction.EXECUTE);
            fuelDebt -= 1f;
        }

    }

    void refreshCapability() {
        fluidCapability = handlerForCapability();
        invalidateCapabilities();
    }
    private IFluidHandler handlerForCapability() {
        return isController() ? (tankInventory)
                : ((getControllerBE() != null) ? getControllerBE().handlerForCapability() : new FluidTank(0));
    }

    public void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide())
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public FluidTank getTank() {
        return tankInventory;
    }

    @Override
    public EngineUpgrades getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(EngineUpgrades upgrade) {
        if (this.upgrade == upgrade)
            return;
        this.upgrade = upgrade;
        reActivateSource = true;
        setChanged();
        if (hasLevel() && !level.isClientSide())
            sendData();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    public ModularDieselEngineBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity be = level.getBlockEntity(controller);
        if (be instanceof ModularDieselEngineBlockEntity)
            return (ModularDieselEngineBlockEntity) be;
        return null;
    }

    @Override
    public boolean isController() {
        return controller == null || controller.equals(worldPosition);
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide() && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        refreshCapability();
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        controller = null;
        length = 1;
        reActivateSource = true;

        refreshCapability();
        setChanged();
        sendData();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevHeight = length;

        updateConnectivity = compound.contains("Uninitialized");
        upgrade = EngineUpgrades.get(Identifier.parse(compound.getStringOr("Upgrade", "")));
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = com.jesz.createdieselgenerators.foundation.FluidCompatibility.readBlockPos(compound, "LastKnownPos");
        if (compound.contains("Controller"))
            controller = com.jesz.createdieselgenerators.foundation.FluidCompatibility.readBlockPos(compound, "Controller");

        if (isController()) {
            length = compound.getIntOr("Height", 0);
            com.jesz.createdieselgenerators.foundation.FluidCompatibility.readTank(registries, compound.getCompoundOrEmpty("TankContent"), tankInventory);
            if (tankInventory.getSpace() < 0)
                tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
            analogSignal = compound.contains("AnalogSignal") ? compound.getIntOr("AnalogSignal", 0) : 0;
            fuelDebt = 0f;
            invalidateFuelCache();
        }

        updateCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevHeight != length) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            if (isController())
                tankInventory.setCapacity(1000);
            invalidateRenderBoundingBox();
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.put("LastKnownPos", com.jesz.createdieselgenerators.foundation.FluidCompatibility.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", com.jesz.createdieselgenerators.foundation.FluidCompatibility.writeBlockPos(controller));
        if (isController()) {
            compound.putString("Upgrade", upgrade.getId().toString());
            compound.put("TankContent", com.jesz.createdieselgenerators.foundation.FluidCompatibility.writeTank(registries, tankInventory));
            compound.putInt("Height", length);
            compound.putInt("AnalogSignal", analogSignal);
        }
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        reActivateSource = true;
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getBlockState().getValue(FACING).getAxis();
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        return 21;
    }

    @Override
    public int getMaxWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public void setHeight(int height) {
        length = height;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public boolean enabled() {
        if (!IEngine.super.enabled())
            return false;
        if (CDGConfig.ANALOG_SPEED_CONTROL.get())
            return true;
        if (!CDGConfig.ENGINES_DISABLED_WITH_REDSTONE.get())
            return true;
        for (int i = 1; i < length; i++) {
            BlockState state = level.getBlockState(getBlockPos().relative(getMainConnectionAxis(), i));
            if (CDGBlocks.MODULAR_DIESEL_ENGINE.has(state))
                if (state.getValue(DieselEngineBlock.POWERED))
                    return false;
        }
        return true;
    }

    @Override
    public int getAnalogSignal() {
        return analogSignal;
    }

    public void setAnalogSignal(int newSignal) { analogSignal = newSignal; }
    public void setSignalChanged(boolean newSignal) { signalChanged = newSignal; }

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

