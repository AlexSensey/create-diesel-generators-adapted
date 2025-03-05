package com.jesz.createdieselgenerators.content.fluid_coupling;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.content.fluid_coupling.FluidCouplingBlock.INPUT;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class FluidCouplingBlockEntity extends GeneratingKineticBlockEntity {

    WeakReference<FluidCouplingBlockEntity> coupledBE = new WeakReference<>(null);

    public FluidCouplingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    SmartFluidTankBehaviour tank;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
    }

    float generatedSpeed = 0;
    float generatedCapacity = 0;
    float lastStressAvailable = 0;
    float soundRotations = 0;
    @Override
    public void tick() {
        super.tick();

        FluidCouplingBlockEntity coupled = coupledBE.get();
        if(coupled == null) {
            generatedSpeed = 0;
            generatedCapacity = 0;
            updateGeneratedRotation();
            return;
        }

        if(soundRotations >= 1 && getBlockState().getValue(INPUT) && level.isClientSide){
            AllSoundEvents.MIXING.playAt(level, worldPosition, 1f, 0.7f, true);
            soundRotations = 0;
        }
        soundRotations += Math.abs(getSpeed()) / 128;

        if(coupled.getOrCreateNetwork() == getOrCreateNetwork()) {
            generatedCapacity = 0;
            generatedSpeed = 0;
            updateGeneratedRotation();
            return;
        }
        if(getBlockState().getValue(INPUT)) {
            coupled.generatedSpeed = getSpeed() * 0.9f;
            if(coupled.generatedSpeed != 0) {
                coupled.generatedCapacity += 1f;
                coupled.generatedCapacity = Math.min((lastStressAvailable / Math.abs(getSpeed())) * 2, coupled.generatedCapacity);
            } else
                coupled.generatedCapacity = 0;

            coupled.updateGeneratedRotation();
            KineticNetwork network = getOrCreateNetwork();
            if(network != null)
                network.updateStressFor(this, calculateStressApplied());
        }
    }
    boolean isController(){
        FluidCouplingBlockEntity coupled = coupledBE.get();
        if(coupled == null)
            return false;
        return coupled.getBlockState().getValue(FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE;
    }
    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        lastStressAvailable = maxStress - currentStress;
    }

    @Override
    public void initialize() {
        super.initialize();
        updateCoupledBE();
    }

    @Override
    public float calculateStressApplied() {
        FluidCouplingBlockEntity coupled = coupledBE.get();
        float impact = 0;
        if(coupled != null && getBlockState().getValue(INPUT))
            impact = coupled.generatedCapacity;

        lastStressApplied = impact;
        return impact;
    }

    @Override
    public float calculateAddedStressCapacity() {
        if(getBlockState().getValue(INPUT)) {
            lastCapacityProvided = 0;
            return 0;
        }
        lastCapacityProvided = generatedCapacity;
        return generatedCapacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if(getBlockState().getValue(INPUT))
            return 0;
        return generatedSpeed;
    }

    public void updateCoupledBE() {
        BlockEntity otherEntity = level.getBlockEntity(worldPosition.relative(getBlockState().getValue(FACING).getOpposite()));
        if(otherEntity instanceof FluidCouplingBlockEntity be && be.getBlockState().getValue(FACING) == getBlockState().getValue(FACING).getOpposite()) {
            coupledBE = new WeakReference<>(be);
            if(be.getBlockState().getValue(INPUT) == getBlockState().getValue(INPUT))
                level.setBlock(be.getBlockPos(), be.getBlockState().setValue(INPUT, !getBlockState().getValue(INPUT)), 3);
        }
    }

}


