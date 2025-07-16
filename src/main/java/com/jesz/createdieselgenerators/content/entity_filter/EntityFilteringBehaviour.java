package com.jesz.createdieselgenerators.content.entity_filter;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class EntityFilteringBehaviour extends FilteringBehaviour {
    public static final BehaviourType<FilteringBehaviour> TYPE = new BehaviourType<>();

    public EntityFilteringBehaviour(SmartBlockEntity be, ValueBoxTransform slot) {
        super(be, slot);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        Level level = getWorld();
        BlockPos pos = getPos();
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack toApply = itemInHand.copy();

        if (AllItems.WRENCH.isIn(toApply))
            return;
        if (AllBlocks.MECHANICAL_ARM.isIn(toApply))
            return;
        if (level.isClientSide())
            return;

        if (getFilter(side).getItem() instanceof EntityFilterItem) {
            if (!player.isCreative() || ItemHelper
                    .extract(new InvWrapper(player.getInventory()),
                            stack -> ItemStack.isSameItemSameComponents(stack, getFilter(side)), true)
                    .isEmpty())
                player.getInventory()
                        .placeItemBackInInventory(getFilter(side));
        }

        if (toApply.getItem() instanceof EntityFilterItem)
            toApply.setCount(1);

        if (!setFilter(side, toApply)) {
            player.displayClientMessage(CreateLang.translateDirect("logistics.filter.invalid_item"), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }

        if (!player.isCreative()) {
            if (toApply.getItem() instanceof EntityFilterItem) {
                if (itemInHand.getCount() == 1)
                    player.setItemInHand(hand, ItemStack.EMPTY);
                else
                    itemInHand.shrink(1);
            }
        }

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);

    }

    @Override
    public boolean setFilter(ItemStack stack) {
        if(stack.getItem() instanceof FilterItem)
            return false;
        if(stack.getItem() instanceof EntityFilterItem || stack.isEmpty())
            return super.setFilter(stack);
        return false;
    }

    @Override
    public void destroy() {
        if (getFilter().getItem() instanceof EntityFilterItem) {
            Vec3 pos = VecHelper.getCenterOf(getPos());
            Level level = getWorld();
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, getFilter().copy()));
        }
        super.destroy();
    }
}
