package com.jesz.createdieselgenerators.content.tools.lighter;

import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class LighterItem extends Item implements FueledToolItem {
    public LighterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        createTooltip(tooltipComponents, stack);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        FluidStack fStack = readFluid(stack);

        var registry = level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE);

        boolean flammable = FuelType.getTypeFor(registry, fStack.getFluid()).isFlammable();
        LighterState state = stack.get(CDGDataComponents.LIGHTER_STATE);
        if (!flammable && state == LighterState.OPEN_IGNITED){
            stack.set(CDGDataComponents.LIGHTER_STATE, LighterState.OPEN);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);

        level.playSound(player, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        if (!stackInHand.has(CDGDataComponents.LIGHTER_STATE) ||
                stackInHand.get(CDGDataComponents.LIGHTER_STATE) == LighterState.CLOSED) {
            if (player.isShiftKeyDown()) {
                stackInHand.set(CDGDataComponents.LIGHTER_STATE, LighterState.OPEN);
                return InteractionResultHolder.success(stackInHand);
            }
            FluidStack fStack = readFluid(stackInHand);

            var registry = level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE);

            boolean flammable = FuelType.getTypeFor(registry, fStack.getFluid()).isFlammable();
            stackInHand.set(CDGDataComponents.LIGHTER_STATE, flammable ? LighterState.OPEN_IGNITED : LighterState.OPEN);

            if (flammable) {
                fStack.setAmount(fStack.getAmount() - 1);
                writeFluid(stackInHand, fStack);
            }
            return InteractionResultHolder.success(stackInHand);
        }
        stackInHand.set(CDGDataComponents.LIGHTER_STATE, LighterState.CLOSED);

        return InteractionResultHolder.success(stackInHand);

    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        ItemStack stack = context.getItemInHand();

        if (stack.get(CDGDataComponents.LIGHTER_STATE) != LighterState.OPEN_IGNITED)
            return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();

        var registry = level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE);

        if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) &&
                !CandleCakeBlock.canLight(blockstate) && !blockstate.is(CDGTags.LIGHTER_LIGHTABLE)) {
            BlockPos blockpos1 = blockpos.relative(context.getClickedFace());
            if (BaseFireBlock.canBePlacedAt(level, blockpos1, context.getHorizontalDirection())) {
                level.playSound(player, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState blockstate1 = BaseFireBlock.getState(level, blockpos1);
                level.setBlock(blockpos1, blockstate1, 11);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos1, stack);
                }

                FluidStack fStack = readFluid(stack);
                if (fStack.getAmount() == 0) {
                    stack.set(CDGDataComponents.LIGHTER_STATE, LighterState.OPEN);
                    return InteractionResult.FAIL;
                }

                boolean flammable = FuelType.getTypeFor(registry, fStack.getFluid()).normal().speed() != 0;
                if (flammable && stack.get(CDGDataComponents.LIGHTER_STATE) == LighterState.OPEN_IGNITED) {
                    fStack.setAmount(fStack.getAmount()-1);
                    writeFluid(stack, fStack);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
            }
        } else {
            level.playSound(player, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            if(blockstate.hasProperty(BlockStateProperties.LIT))
                level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, true), 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);

            FluidStack fStack = readFluid(stack);
            if (fStack.getAmount() == 0){
                stack.set(CDGDataComponents.LIGHTER_STATE, LighterState.OPEN);
                return InteractionResult.FAIL;
            }

            boolean flammable = FuelType.getTypeFor(registry, fStack.getFluid()).normal().speed() != 0;

            if (flammable && stack.get(CDGDataComponents.LIGHTER_STATE) == LighterState.OPEN_IGNITED) {
                fStack.setAmount(fStack.getAmount()-1);
                writeFluid(stack, fStack);
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCurrentFillLevel(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (float) getCurrentFillLevel(stack) / getCapacity(stack));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        if (!item.is(CDGItems.LIGHTER.get()) ||
                !CDGConfig.COMBUSTIBLES_BLOW_UP.get() ||
                item.get(CDGDataComponents.LIGHTER_STATE) != LighterState.OPEN_IGNITED)
            return false;

        Vec3 entityPos = itemEntity.getPosition(1);
        FluidState fState = itemEntity.level().getFluidState(new BlockPos(BlockPos.containing(entityPos)));
        if (fState.is(Fluids.WATER) || fState.is(Fluids.FLOWING_WATER)) {
            item.set(CDGDataComponents.LIGHTER_STATE, LighterState.OPEN);
            itemEntity.level().playLocalSound(entityPos.x, entityPos.y, entityPos.z, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f, false);
        } else {
            boolean flammable = FuelType.getTypeFor(itemEntity.level().registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fState.getType()).normal().speed() != 0;
            if (flammable)
                itemEntity.level().explode(null, null, null, entityPos.x, entityPos.y, entityPos.z, 1, true, Level.ExplosionInteraction.BLOCK);
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void registerExtension(RegisterClientExtensionsEvent event) {
        event.registerItem(SimpleCustomRenderer.create(this, new LighterItemRenderer()), this);
    }
}
