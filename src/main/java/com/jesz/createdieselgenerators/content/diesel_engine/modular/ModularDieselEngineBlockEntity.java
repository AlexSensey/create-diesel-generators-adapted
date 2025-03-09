package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.*;

public class ModularDieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine {
    public ModularDieselEngineBlockEntity controller = null;
    BlockPos controllerPos = null;
    int tick;
    public int length;
    EngineUpgrades upgrade = EngineUpgrades.NONE;

    public ModularDieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public SmartFluidTankBehaviour tank;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (computerBehaviour.isPeripheralCap(cap))
            return computerBehaviour.getPeripheralCapability();
        if (getBlockState().getValue(PIPE)) {
            ModularDieselEngineBlockEntity controller = this.controller;
            if (cap == ForgeCapabilities.FLUID_HANDLER && (side == Direction.UP || side == null))
                if (controller != null)
                    return controller.tank.getCapability().cast();
                else
                    return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("Tick", tick);
        if(controllerPos != null)
            compound.put("Controller", NbtUtils.writeBlockPos(controllerPos));
        if(clientPacket)
            compound.putFloat("Pitch", targetStressPitch);
        compound.putString("Upgrade", upgrade.getId().toString());
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tick = compound.getInt("Tick");
        controllerPos = NbtUtils.readBlockPos(compound.getCompound("Controller"));
        if(clientPacket)
            targetStressPitch = compound.getFloat("Pitch");
        upgrade = EngineUpgrades.NONE;
        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades){
            if(upgrade.getId().toString().equals(compound.getString("Upgrade"))){
                this.upgrade = upgrade;
                break;
            }
        }
    }
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    public AbstractComputerBehaviour computerBehaviour;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));

        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new ModularDieselEngineValueBox());
        movementDirection.withCallback($ -> onDirectionChanged());

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }
    public void onDirectionChanged() {
        ModularDieselEngineBlockEntity controller = this.controller;
        if(controller == null)
            return;
        ModularDieselEngineBlockEntity lastEngine = getBackEngine();
        if(lastEngine == null)
            return;
        while (lastEngine != null) {
            lastEngine.movementDirection.setValue(movementDirection.getValue());
            lastEngine = lastEngine.getBackEngine();
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        updateConnectivity();
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (getGeneratedSpeed() == 0)
            capacity = 0;
        else if(!validFS())
            capacity = 0;
        else if(!enabled())
            capacity = 0;
        else
            capacity = upgrade.getStress(getFuelStress() * length, this);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {

        if(!validFS() || controller != this)
            return 0;
        if(!enabled())
            return 0;

        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1) * upgrade.getSpeed(IEngine.super.getFuelSpeed(), this), getBlockState().getValue(ModularDieselEngineBlock.FACING));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        ModularDieselEngineBlockEntity frontEngine = this.controller;
        if (!StressImpact.isEnabled() || frontEngine == null)
            return added;
        float stressBase = frontEngine.calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;
        if(frontEngine != this){
            CreateLang.translate("gui.goggles.generator_stats")
                    .forGoggles(tooltip);
            CreateLang.translate("tooltip.capacityProvided")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            float stressTotal = Math.abs(frontEngine.getGeneratedSpeed()* stressBase);

            CreateLang.number(stressTotal)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .add(CreateLang.translate("gui.goggles.at_current_speed")
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

        }
        return containedFluidTooltip(tooltip, isPlayerSneaking, frontEngine.tank.getCapability().cast());
    }
    int soundCounter = 0;
    float currentStressPitch = 0;
    float targetStressPitch = 0;
    float lastSpeed = 0;
    float lastCapacity = 0;
    @Override
    public void tick() {
        super.tick();
        currentStressPitch = Mth.lerp(0.2f, currentStressPitch, targetStressPitch);


        if (getGeneratedSpeed() != lastSpeed || lastCapacity != calculateAddedStressCapacity()) {
            reActivateSource = true;
            lastCapacityProvided = lastCapacity;
            lastCapacity = calculateAddedStressCapacity();
            lastSpeed = getGeneratedSpeed();
        }

        ModularDieselEngineBlockEntity controller = this.controller;
        if(controller == null)
            return;
        if(controller != this) {
            tank.getPrimaryHandler().drain(controller.tank.getPrimaryHandler().fill(tank.getPrimaryHandler().getFluid(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            if (upgrade != EngineUpgrades.NONE && controller.upgrade == EngineUpgrades.NONE) {
                controller.upgrade = upgrade;
                upgrade = EngineUpgrades.NONE;
            }
        }
        if (controller.validFS() && controller.enabled() && (controller == this || (worldPosition.hashCode() == 11))) {
            tickFuelUsage(length);
            controller.upgrade.playSounds(tick, this);
        }
        tick++;
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
    public ModularDieselEngineBlockEntity getBackEngine() {
        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        if(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            facing = facing.getOpposite();
        ModularDieselEngineBlockEntity be = level.getBlockEntity(worldPosition.relative(facing), CDGBlockEntityTypes.LARGE_DIESEL_ENGINE.get()).orElse(null);
        return be == null ? null : be.getBlockState().getValue(FACING).getAxis() != getBlockState().getValue(FACING).getAxis() ? null : be;
    }
    public ModularDieselEngineBlockEntity getFrontEngine() {
        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        if(facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
            facing = facing.getOpposite();
        ModularDieselEngineBlockEntity be = level.getBlockEntity(worldPosition.relative(facing), CDGBlockEntityTypes.LARGE_DIESEL_ENGINE.get()).orElse(null);
        return be == null ? null : be.getBlockState().getValue(FACING).getAxis() != getBlockState().getValue(FACING).getAxis() ? null : be;
    }
    public void updateConnectivity(){
        ModularDieselEngineBlockEntity frontEngine = getFrontEngine();
        if(frontEngine != null){
            frontEngine.updateConnectivity();
            return;
        }
        controller = this;
        controllerPos = worldPosition;
        ModularDieselEngineBlockEntity backEngine = getBackEngine();
        if(backEngine == null){
            length = 1;
            return;
        }
        ModularDieselEngineBlockEntity lastEngine = backEngine;
        int length = 1;
        for (;lastEngine != null; length++) {
            lastEngine = lastEngine.getBackEngine();
        }
        this.length = length;
        lastEngine = backEngine;
        while (lastEngine != null) {
            lastEngine.length = length;
            lastEngine.controller = controller;
            lastEngine.controllerPos = controllerPos;
            lastEngine = lastEngine.getBackEngine();
        }
    }
    public void removed() {
        ModularDieselEngineBlockEntity lastEngine = getBackEngine();
        while (lastEngine != null) {
            tank.getPrimaryHandler().drain(lastEngine.tank.getPrimaryHandler().fill(tank.getPrimaryHandler().getFluid(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            lastEngine.length = 1;
            lastEngine.controller = null;
            lastEngine = lastEngine.getBackEngine();
        }
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

    @Override
    public void playSound() {
        if (level.isClientSide && Minecraft.getInstance().player.position().distanceTo(Vec3.atBottomCenterOf(worldPosition)) < 15)
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), CDGSounds.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, 0.3f,1f + currentStressPitch, false);
    }
}

