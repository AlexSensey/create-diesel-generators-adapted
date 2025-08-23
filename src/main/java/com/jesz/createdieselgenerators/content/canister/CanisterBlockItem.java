package com.jesz.createdieselgenerators.content.canister;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.simibubi.create.AllEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class CanisterBlockItem extends BlockItem implements FueledToolItem {
    public CanisterBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        createTooltip(tooltipComponents, stack);
    }

    @Override
    public int getBaseCapacity(ItemStack stack) {
        return CDGConfig.CANISTER_CAPACITY.get();
    }

    @Override
    public int getCapacityEnchantmentAddition(ItemStack stack) {
        return CDGConfig.CANISTER_CAPACITY_ENCHANTMENT.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return super.useOn(p_40581_);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (AllEnchantments.CAPACITY.equals(enchantment.getKey()))
            return true;
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCurrentFillLevel(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (float) getCurrentFillLevel(stack) / getCapacity(stack));
    }
}
