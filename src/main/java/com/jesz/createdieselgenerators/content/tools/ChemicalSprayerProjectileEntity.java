package com.jesz.createdieselgenerators.content.tools;

import com.jesz.createdieselgenerators.CDGEntityTypes;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalSprayerProjectileEntity extends AbstractHurtingProjectile {
    public FluidStack stack;
    public boolean fire;
    public boolean cooling;

    public ChemicalSprayerProjectileEntity(EntityType<? extends AbstractHurtingProjectile> type, Level level) {
        super(type, level);
    }
    int t = 0;
    public static ChemicalSprayerProjectileEntity spray(Level level, FluidStack stack, boolean fire, boolean cooling){
        ChemicalSprayerProjectileEntity projectile = new ChemicalSprayerProjectileEntity(CDGEntityTypes.CHEMICAL_SPRAYER_PROJECTILE.get(), level);
        projectile.stack = stack;
        projectile.fire = fire;
        projectile.cooling = cooling;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Fire", fire);
        tag.putBoolean("Cooling", cooling);
        tag.put("FluidStack", new CompoundTag());
        stack.writeToNBT(tag.getCompound("FluidStack"));
        projectile.getEntityData().set(DATA, tag);
        return projectile;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (fire) {
            hit.getEntity().setSecondsOnFire((hit.getEntity().getRemainingFireTicks() / 20) + 10);
            hit.getEntity().hurt(damageSources().inFire(), 2);
        } else if(cooling)
            hit.getEntity().clearFire();
        else if (stack.getFluid().isSame(AllFluids.POTION.get())) {
            if (hit.getEntity() instanceof LivingEntity le && le.isAffectedByPotions()) {
                for (MobEffectInstance effectInstance : PotionUtils.getMobEffects(PotionFluidHandler.fillBottle(new ItemStack(Items.GLASS_BOTTLE), stack))){
                    MobEffect effect = effectInstance.getEffect();
                    if (effect.isInstantenous()) {
                        effect.applyInstantenousEffect(null, null, le, effectInstance.getAmplifier(), 0.5d);
                    } else {
                        le.addEffect(new MobEffectInstance(effectInstance));
                    }
                }
            }
        } else if (FluidHelper.isTag(stack, Tags.Fluids.MILK)){
            if (hit.getEntity() instanceof LivingEntity le && le.isAffectedByPotions()) {
                ItemStack curativeItem = new ItemStack(Items.MILK_BUCKET);
                le.curePotionEffects(curativeItem);
            }
        } else
            hit.getEntity().hurt(damageSources().generic(), 0.5f);

        super.onHitEntity(hit);
        remove(RemovalReason.DISCARDED);
    }

    @Override
    public void load(CompoundTag compound) {
        if (stack == null)
            stack = FluidStack.loadFluidStackFromNBT(compound.getCompound("FluidStack"));
        super.load(compound);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag compound) {
        if(stack != null)
            stack.writeToNBT(compound.getCompound("FluidStack"));
        return super.saveWithoutId(compound);
    }
    static final EntityDataAccessor<CompoundTag> DATA = SynchedEntityData.defineId(ChemicalSprayerProjectileEntity.class, EntityDataSerializers.COMPOUND_TAG);
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Fire", fire);
        tag.putBoolean("Cooling", cooling);
        tag.put("FluidStack", new CompoundTag());
        FluidStack.EMPTY.writeToNBT(tag.getCompound("FluidStack"));
        this.entityData.define(DATA, tag);
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            stack = FluidStack.loadFluidStackFromNBT(getEntityData().get(DATA).getCompound("FluidStack"));
            fire = getEntityData().get(DATA).getBoolean("Fire");
            cooling = getEntityData().get(DATA).getBoolean("Cooling");
            if (stack != null && !stack.isEmpty() && !fire)
                level().addParticle(FluidFX.getFluidParticle(stack), position().x+random.nextDouble()-0.5, position().y+0.3, position().z+random.nextDouble()-0.5, getDeltaMovement().x, getDeltaMovement().y - 0.1, getDeltaMovement().z);
            if (t >= 1) {
                if (fire) {
                    level().addParticle(ParticleTypes.LAVA, position().x, position().y, position().z, getDeltaMovement().x, getDeltaMovement().y - 0.1, getDeltaMovement().z);
                }
                t = 0;
            }
            else
                t++;
        }
        setDeltaMovement(getDeltaMovement().add(0, -0.015, 0));

        if (fire) {

            Fluid fluid = level().getFluidState(BlockPos.containing(position())).getType();
            boolean flammable = FuelType.getTypeFor(level().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluid).normal().speed() != 0;

            if (flammable)
                level().explode(null, getX(), getY(), getZ(), 3, Level.ExplosionInteraction.BLOCK);
            else if (level().getFluidState(new BlockPos((int) getPosition(1).x, (int) getPosition(1).y, (int) getPosition(1).z)).is(Fluids.FLOWING_WATER) || level().getFluidState(new BlockPos((int) getPosition(1).x, (int) getPosition(1).y, (int) getPosition(1).z)).is(Fluids.WATER)) {
                fire = false;
                if(stack.getFluid().isSame(Fluids.LAVA))
                    remove(RemovalReason.DISCARDED);
                getEntityData().get(DATA).putBoolean("Fire", false);
            }
        }


        Entity entity = this.getOwner();
        if (this.level().isClientSide || (entity == null || !entity.isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
            if (this.shouldBurn()) {
                this.setSecondsOnFire(1);
            }

            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
                this.onHit(hitresult);
            }

            this.checkInsideBlocks();
            Vec3 vec3 = this.getDeltaMovement();
            double d0 = this.getX() + vec3.x;
            double d1 = this.getY() + vec3.y;
            double d2 = this.getZ() + vec3.z;
            ProjectileUtil.rotateTowardsMovement(this, 0.2F);
            float f = this.getInertia();
            if (this.isInWater())
                f = 0.8F;


            this.setDeltaMovement(vec3.add(this.xPower, this.yPower, this.zPower).scale(f));
            this.setPos(d0, d1, d2);
        } else {
            this.discard();
        }
    }

    @Override
    public boolean isOnFire() {
        return fire;
    }
    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        BlockPos pos = new BlockPos((int) Math.floor(getPosition(1).x), (int) Math.floor(getPosition(1).y), (int) Math.floor(getPosition(1).z));
        if(cooling) {
            if (level().getBlockState(pos).getBlock() instanceof FireBlock) {
                level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                level().playLocalSound(position().x, position().y, position().z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2, true);
            }

            for (int i = 0; i < 6; i++) {
                if (level().getBlockState(pos.relative(Direction.values()[i], 1)).getBlock() instanceof FireBlock) {
                    level().setBlock(pos.relative(Direction.values()[i], 1), Blocks.AIR.defaultBlockState(), 3);
                    level().playLocalSound(position().x, position().y, position().z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2, true);
                }
            }
        }
        if(fire && level().getBlockState(pos).getBlock() instanceof AirBlock && BlockHelper.hasBlockSolidSide(level().getBlockState(pos.below()), level(), pos.below(), Direction.UP))
            level().setBlock(pos, FireBlock.getState(level(), pos), 3);
        remove(RemovalReason.DISCARDED);
    }

    @Override
    public float getPickRadius() {
        return 0.0f;
    }
}
