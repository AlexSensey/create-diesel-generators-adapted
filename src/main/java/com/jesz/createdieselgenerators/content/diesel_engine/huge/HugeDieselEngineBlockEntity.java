package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGSounds;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class HugeDieselEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IEngine {
    public WeakReference<PoweredEngineShaftBlockEntity> target = new WeakReference<>(null);
    public SmartFluidTankBehaviour tank;
    EngineUpgrades upgrade = EngineUpgrades.NONE;
    public HugeDieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

    }
    int tick;

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        tank.write(compound, clientPacket);
        compound.putInt("Tick", tick);
        compound.putString("Upgrade", upgrade.getId().toString());

    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tank.read(compound, clientPacket);
        tick = compound.getInt("Tick");
        upgrade = EngineUpgrades.NONE;
        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades){
            if(upgrade.getId().toString().equals(compound.getString("Upgrade"))){
                this.upgrade = upgrade;
                break;
            }
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }
    float oldAngle = 0;
    @Override
    public void tick() {
        super.tick();
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if (shaft == null)
            return;

        if (enabled()) {
            tickFuelUsage();
            tick++;

            if (shaft.movementDirection != 0 && shaft.movementDirection != (movementDirection.get() == WindmillBearingBlockEntity.RotationDirection.CLOCKWISE ? 1 : -1)) {
                shaft.removeGenerator(worldPosition);
                onDirectionChanged();
                return;
            }
            shaft.update(worldPosition, movementDirection.getValue() == 0 ? 1 : -1, upgrade.getStress(upgrade.getStress(getFuelStress(), this), this), upgrade.getSpeed(getFuelSpeed(), this));
            if (!level.isClientSide)
                return;
            Float angle = getTargetAngle();
            if (angle == null)
                return;
            angle = (float) (angle*180/Math.PI);
            angle = angle < 0 ? 360-angle : angle;
            Direction facing = getBlockState().getValue(FACING);
            float shaftR = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? 0 : facing == Direction.WEST ? 180 : facing == Direction.DOWN ? 90 : -90;

            if ((oldAngle+shaftR) % 360 > (angle+shaftR) % 360) {
                upgrade.playSounds(0, this);
            }
            oldAngle = angle;

        } else {
            shaft.removeGenerator(worldPosition);
        }
    }

    public PoweredEngineShaftBlockEntity getShaft() {

        PoweredEngineShaftBlockEntity shaft = target.get();
        if (shaft == null || shaft.isRemoved() || !shaft.canBePoweredBy(worldPosition)) {
            if (shaft != null)
                target = new WeakReference<>(null);
            BlockEntity anyShaftAt = level.getBlockEntity(worldPosition.relative(getBlockState().getValue(FACING), 2));
            BlockState sState = level.getBlockState(worldPosition.relative(getBlockState().getValue(FACING), 2));
            if (anyShaftAt instanceof PoweredEngineShaftBlockEntity ps)
                target = new WeakReference<>(shaft = ps);
            else if(sState.getBlock() instanceof ShaftBlock)
                if(sState.getValue(AXIS) != getBlockState().getValue(FACING).getAxis())
                    level.setBlock(worldPosition.relative(getBlockState().getValue(FACING), 2), PoweredEngineShaftBlock.getEquivalent(level.getBlockState(worldPosition.relative(getBlockState().getValue(FACING), 2))), 3);
        }
        return shaft;
    }
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    public AbstractComputerBehaviour computerBehaviour;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new HugeDieselEngineValueBox());
        movementDirection.withCallback($ -> onDirectionChanged());

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 100);
        behaviours.add(tank);
    }

    private void onDirectionChanged() {
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if(shaft == null)
            return;
        for (Pair<BlockPos, Couple<Float>> engine : shaft.engines)
            if(level.getBlockEntity(engine.getFirst()) instanceof HugeDieselEngineBlockEntity be)
                be.movementDirection.setValue(movementDirection.getValue());
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER && side == null)
            return tank.getCapability().cast();
        else if (cap == ForgeCapabilities.FLUID_HANDLER && side.getAxis() != getBlockState().getValue(FACING).getAxis())
            return tank.getCapability().cast();

        return super.getCapability(cap, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!IRotate.StressImpact.isEnabled() || !enabled())
            return false;
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if(shaft == null)
            return false;
        float stressBase = FuelTypeManager.getGeneratedStress(this, tank.getPrimaryHandler().getFluid().getFluid());
        if (Mth.equal(stressBase, 0))
            return false;
        CreateLang.translate("gui.goggles.generator_stats")
                .forGoggles(tooltip);
        CreateLang.translate("tooltip.capacityProvided")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float stressTotal = Math.abs(stressBase);

        CreateLang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(CreateLang.translate("gui.goggles.at_current_speed")
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
    }


    @OnlyIn(Dist.CLIENT)
    public Float getTargetAngle() {
        float angle;
        BlockState state = getBlockState();
        if (!CDGBlocks.HUGE_DIESEL_ENGINE.has(state))
            return null;

        Direction facing = state.getValue(FACING);
        PoweredEngineShaftBlockEntity shaft = getShaft();
        Direction.Axis facingAxis = facing.getAxis();
        Direction.Axis axis;

        if (shaft == null)
            return null;

        axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getBlockPos(), axis);
        if (axis == facingAxis)
            return null;
        if (axis.isHorizontal() && (facingAxis == Direction.Axis.X ^ facing.getAxisDirection() == Direction.AxisDirection.POSITIVE))
            angle *= -1;
        if (axis == Direction.Axis.X && facing == Direction.DOWN)
            angle *= -1;
        return angle;
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
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), CDGSounds.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, 0.3f,1f, false);
    }
}
