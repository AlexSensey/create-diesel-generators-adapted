package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
//import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringBehaviour;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilterItem;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringBehaviour;
import com.jesz.createdieselgenerators.mixin_interfaces.IEntity;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TurretBlockEntity extends KineticBlockEntity {

    public float oldHorizontalRotation;
    public float oldVerticalRotation;
    public float oldTargetedVerticalRotation = 0;
    public float oldTargetedHorizontalRotation = 0;

    public float horizontalRotation;
    public float verticalRotation;
    public float targetedVerticalRotation = 0;
    public float targetedHorizontalRotation = 0;

    public Player controllingPlayer;
    public LivingEntity controllingEntity;
    public Entity targetedEntity;
    public Direction controllingEntityDirection;
    boolean removePlayer = false;

    public TurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    int t;

    @Override
    public void tick() {
        if(controllingEntity == null )
            targetedEntity = null;
        if (controllingEntity != null){
            controllingEntity.setYHeadRot(-targetedHorizontalRotation+180);
            if (controllingEntity.getRootVehicle() instanceof SeatEntity seat) {
                if (Math.sqrt(seat.blockPosition().distSqr(worldPosition)) > 1) {
                    ((IEntity)controllingEntity).setTurretPos(null);
                    controllingEntity = null;
                }
            } else {
                ((IEntity) controllingEntity).setTurretPos(null);
                controllingEntity = null;
            }
        }else {
            if(t==0)
                for(Direction direction : Direction.Plane.HORIZONTAL){
                    List<SeatEntity> list = level.getEntitiesOfClass(SeatEntity.class, new AABB(getBlockPos().relative(direction)));
                    if(!list.isEmpty()){
                        List<Entity> passengers = list.get(0).getPassengers();
                        if(!passengers.isEmpty() && !(passengers.get(0) instanceof Player)) {
                            Entity possibleControllingEntity = passengers.get(0);
                            if(((IEntity)possibleControllingEntity).getTurretPos() == null) {
                                ((IEntity)possibleControllingEntity).setTurretPos(worldPosition);
                                if (possibleControllingEntity instanceof LivingEntity le) {
                                    controllingEntity = le;
                                    controllingEntityDirection = direction;
                                }
                            }
                        }
                    }
                }
        }
        super.tick();
        if(controllingEntity != null && controllingPlayer == null && targetedEntity != null){
            AABB aabb = new AABB(worldPosition.getX() - (controllingEntityDirection == Direction.WEST ? -3 : 33), worldPosition.getY() - 3.6, worldPosition.getZ() - (controllingEntityDirection == Direction.NORTH ? -3 : 33),
                    worldPosition.getX() + (controllingEntityDirection == Direction.EAST ? -3 : 33), worldPosition.getY() + 2, worldPosition.getZ() + (controllingEntityDirection == Direction.SOUTH ? -3 : 33));

            targetedHorizontalRotation = (float) (Math.atan2(targetedEntity.getX() - worldPosition.getX() - 0.5f, targetedEntity.getZ() - worldPosition.getZ() - 0.5f) * 180 / Math.PI) + 180;
            targetedVerticalRotation = (float) Mth.clamp(Math.atan2(targetedEntity.getY() - worldPosition.getY() - 0.5f, Math.sqrt(
                    (targetedEntity.getZ() - worldPosition.getZ() - 0.5f) * (targetedEntity.getZ() - worldPosition.getZ() - 0.5f) +
                            (targetedEntity.getX() - worldPosition.getX() - 0.5f) * (targetedEntity.getX() - worldPosition.getX() - 0.5f)
            )) * -180 / Math.PI -3, -50, 11);

            if (!aabb.contains(targetedEntity.position()) || targetedEntity.isRemoved() || targetedEntity.getPosition(1).distanceTo(Vec3.atCenterOf(worldPosition)) > 44 || targetedEntity.getPosition(1).distanceTo(Vec3.atCenterOf(worldPosition)) < 3
                    || targetedEntity.getY() < worldPosition.getY() - 3 || targetedEntity.getY() > worldPosition.getY() + 3)
                targetedEntity = null;

        }
        t++;
        if (t >= 40) {
            t = 0;
            updateTargetedEntity();
        }

        oldHorizontalRotation = horizontalRotation;
        oldVerticalRotation = verticalRotation;
        horizontalRotation = AngleHelper.angleLerp(0.1f, horizontalRotation, targetedHorizontalRotation);
        verticalRotation = AngleHelper.angleLerp(0.1f, verticalRotation, targetedVerticalRotation);
        if(oldTargetedHorizontalRotation != targetedHorizontalRotation || oldTargetedVerticalRotation != targetedVerticalRotation)
            sendData();
        oldTargetedHorizontalRotation = targetedHorizontalRotation;
        oldTargetedVerticalRotation = targetedVerticalRotation;
        if(controllingPlayer == null)
            return;
        if(Math.sqrt(controllingPlayer.distanceToSqr(Vec3.atCenterOf(worldPosition))) > 3 || controllingPlayer.isCrouching())
            removePlayer();
        if(removePlayer || controllingPlayer.isRemoved()){
            controllingPlayer = null;
            removePlayer = false;
            return;
        }
        targetedVerticalRotation = Mth.clamp(controllingPlayer.xRotO, -50, 1);
        targetedHorizontalRotation = -controllingPlayer.yHeadRotO+180;
    }

    @Override
    public void remove() {
        super.remove();
        if(controllingEntity != null)
            ((IEntity)controllingEntity).setTurretPos(null);
    }

    public void updateTargetedEntity(){
        AABB aabb = new AABB(worldPosition.getX() - (controllingEntityDirection == Direction.WEST ? -3 : 33), worldPosition.getY() - 3.6, worldPosition.getZ() - (controllingEntityDirection == Direction.NORTH ? -3 : 33),
                worldPosition.getX() + (controllingEntityDirection == Direction.EAST ? -3 : 33), worldPosition.getY() + 2, worldPosition.getZ() + (controllingEntityDirection == Direction.SOUTH ? -3 : 33));
//        level.getNearestEntity(level.getEntitiesOfClass(LivingEntity.class, aabb), TargetingConditions.forNonCombat(), controllingEntity, controllingEntity.getX(), controllingEntity.getEyeY(), controllingEntity.getZ());
        List<Entity> entities = level.getEntities(null, aabb).stream().filter(e -> {

            if (!(e instanceof LivingEntity))
                return false;

            if (!TargetingConditions.forCombat().test(controllingEntity, (LivingEntity) e))
                return false;
            if (e.isRemoved() || e.getPosition(1).distanceTo(Vec3.atCenterOf(worldPosition)) > 44 || e.getPosition(1).distanceTo(Vec3.atCenterOf(worldPosition)) < 2
                    || e.getY() < worldPosition.getY() - 3 || e.getY() > worldPosition.getY() + 3)
                return false;
            if(filtering.getFilter().getItem() instanceof SpawnEggItem egg)
                if(egg.getType(null) != e.getType())
                    return false;
            if(filtering.getFilter().getItem() instanceof EntityFilterItem) {
                if (!EntityFilterItem.test(filtering.getFilter(), e))
                    return false;
            }else if(e instanceof Player)
                return false;
            return e.getPosition(1).distanceTo(Vec3.atCenterOf(worldPosition)) > 3;
        }).sorted((entity, t1) -> {
            if(entity == targetedEntity)
                return 1;
            if(t1 == targetedEntity)
                return 0;
            return (int) t1.position().distanceTo(Vec3.atCenterOf(worldPosition));
        }).toList();
        if (entities.isEmpty())
            targetedEntity = null;
        else
            targetedEntity = entities.get(0);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        targetedVerticalRotation = compound.getFloat("VerticalRotation");
        targetedHorizontalRotation = compound.getFloat("HorizontalRotation");
    }
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putFloat("VerticalRotation", targetedVerticalRotation);
        compound.putFloat("HorizontalRotation", targetedHorizontalRotation);
    }

    private FilteringBehaviour filtering;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        filtering = new EntityFilteringBehaviour(this, new ChemicalTurretBlockEntity.ChemicalTurretValueBox());
        behaviours.add(filtering);
    }

    public void setControllingPlayer(Player player){
        if(level.isClientSide)
            AllSoundEvents.CONTROLLER_CLICK.play(level, player, worldPosition);
        BlockPos tPos = ((IEntity)player).getTurretPos();
        if(tPos != null && level.getBlockEntity(tPos) instanceof TurretBlockEntity be)
            be.removePlayer();
        if(Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(worldPosition))) > 3){
            if(player instanceof ServerPlayer sp)
                sp.connection.send(new ClientboundSetActionBarTextPacket(CreateDieselGenerators.lang("actionbar.turret.too_far_away")));
            return;
        }
        if(player instanceof ServerPlayer sp)
            sp.connection.send(new ClientboundSetActionBarTextPacket(CreateLang.translateDirect("contraption.controls.start_controlling", Component.translatable(getBlockState().getBlock().getDescriptionId()))));
        controllingPlayer = player;
        ((IEntity)controllingPlayer).setTurretPos(worldPosition);
    }

    public void removePlayer() {
        if(level.isClientSide)
            AllSoundEvents.CONTROLLER_CLICK.play(level, controllingPlayer, worldPosition);
        if(controllingPlayer instanceof ServerPlayer sp)
            sp.connection.send(new ClientboundSetActionBarTextPacket(CreateDieselGenerators.lang("actionbar.turret.stopped_controlling", Component.translatable(getBlockState().getBlock().getDescriptionId()))));
        ((IEntity)controllingPlayer).setTurretPos(null);
        removePlayer = true;
    }
}
