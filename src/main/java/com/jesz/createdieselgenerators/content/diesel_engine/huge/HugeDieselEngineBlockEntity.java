package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

public class HugeDieselEngineBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IEngine {
    ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    float remainingTicks = 0;
    EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    SmartFluidTankBehaviour tank;
    WeakReference<PoweredEngineShaftBlockEntity> target = new WeakReference<>(null);

    public HugeDieselEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

    }
    int tick;

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putInt("Tick", tick);
        tag.putString("Upgrade", upgrade.getId().toString());

    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        tick = tag.getInt("Tick");
        upgrade = EngineUpgrades.get(new ResourceLocation(tag.getString("Upgrade")));
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
            if (remainingTicks < 2)
                remainingTicks += 1 / getFuelBurnRate();

            tank.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);

            if (remainingTicks >= 0)
                remainingTicks--;

            if (shaft.movementDirection != 0 && shaft.movementDirection != (movementDirection.get() == WindmillBearingBlockEntity.RotationDirection.CLOCKWISE ? 1 : -1)) {
                shaft.removeGenerator(worldPosition);
                onDirectionChanged(movementDirection.getValue());
                return;
            }

            shaft.update(worldPosition, movementDirection.getValue() == 0 ? 1 : -1, upgrade.getCapacity(upgrade.getCapacity(getFuelCapacity(), this), this), upgrade.getSpeed(getFuelSpeed(), this));
            if (!level.isClientSide)
                return;

            Float angle = getTargetAngle();
            if (angle == null)
                return;
            angle = (float) (angle * 180 / Math.PI);
            angle = angle < 0 ? 360 - angle : angle;

            Direction facing = getBlockState().getValue(FACING);
            float shaftR = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? 0 : facing == Direction.WEST ? 180 : facing == Direction.DOWN ? 90 : -90;

            if ((oldAngle+shaftR) % 360 > (angle+shaftR) % 360) {
                // TODO: sounds
            }
            oldAngle = angle;

        } else {
            shaft.removeGenerator(worldPosition);
        }
    }

    public PoweredEngineShaftBlockEntity getShaft() {

        PoweredEngineShaftBlockEntity shaft = target.get();
        if (shaft == null || shaft.isRemoved() || !shaft.canBePoweredBy()) {
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
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new HugeDieselEngineValueBox());
        movementDirection.withCallback(this::onDirectionChanged);

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 100);
        behaviours.add(tank);
    }

    private void onDirectionChanged(int v) {
        PoweredEngineShaftBlockEntity shaft = getShaft();
        if(shaft == null)
            return;
        for (Pair<BlockPos, Couple<Float>> engine : shaft.engines)
            if(level.getBlockEntity(engine.getFirst()) instanceof HugeDieselEngineBlockEntity be)
                be.movementDirection.setValue(v);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && (side == null || side.getAxis() != getBlockState().getValue(FACING).getAxis()))
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
    public float getRemainingTicks() {
        return remainingTicks;
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public FluidTank getTank() {
        return tank.getPrimaryHandler();
    }
}
