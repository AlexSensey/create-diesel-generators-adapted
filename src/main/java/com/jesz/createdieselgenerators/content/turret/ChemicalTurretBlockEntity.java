package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerProjectileEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;
import java.util.Random;

public class ChemicalTurretBlockEntity extends TurretBlockEntity {

    public boolean lighterUpgrade = false;
    public ChemicalTurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        return 4;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    public SmartFluidTankBehaviour tank;
    public AbstractComputerBehaviour computerBehaviour;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (computerBehaviour.isPeripheralCap(cap))
            return computerBehaviour.getPeripheralCapability();
        if(side == Direction.DOWN)
            return tank.getCapability().cast();
        return LazyOptional.empty();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
    }
    public int redstoneSignal;
    @Override
    public void tick() {
        super.tick();
        if(redstoneSignal != 0)
            shootFluids();
        if(targetedEntity == null)
            return;
        if(Math.abs(targetedHorizontalRotation - horizontalRotation)%360 <= 4 || Math.abs(targetedHorizontalRotation - horizontalRotation)%360 >= 356)
            shootFluids();
    }
    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        lighterUpgrade = compound.getBoolean("LighterUpgrade");
        redstoneSignal = compound.getInt("RedstoneSignal");
    }
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putBoolean("LighterUpgrade", lighterUpgrade);
        compound.putInt("RedstoneSignal", redstoneSignal);
    }
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }

    public void shootFluids() {
        if(getSpeed() == 0)
            return;
        float shootingForce = (float) Math.min(Math.abs(1 - Math.pow(1 - (getSpeed() / 256), 3)), 1);

        if(!level.isClientSide && !tank.isEmpty()) {
            AllSoundEvents.MIXING.playOnServer(level, worldPosition, .75f, 1);
            FluidStack fluidStack = tank.getPrimaryHandler().getFluid().copy();
            ChemicalSprayerProjectileEntity projectile = ChemicalSprayerProjectileEntity.spray(level, fluidStack, (FuelTypeManager.getGeneratedSpeed(fluidStack.getFluid()) != 0 && lighterUpgrade) || fluidStack.getFluid().isSame(Fluids.LAVA), fluidStack.getFluid().isSame(Fluids.WATER));
            projectile.setPos(Vec3.atCenterOf(worldPosition).add(0, 0.625f, 0));
            projectile.shootFromRotation(projectile, verticalRotation + new Random().nextFloat(-1, 1),
                    (float)(Math.atan2(Math.sin(horizontalRotation/180*Math.PI), -Math.cos(horizontalRotation/180*Math.PI))*180/Math.PI)
                            + new Random().nextFloat(-1, 1), 0.0f, 0.2f+shootingForce, 0);
            level.addFreshEntity(projectile);
            if(t == 1)
                tank.getPrimaryHandler().drain(3, IFluidHandler.FluidAction.EXECUTE);
        }
    }
    public static class ChemicalTurretValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 3, 16.05);
        }
        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis()
                    .isHorizontal();
        }
    }
}
