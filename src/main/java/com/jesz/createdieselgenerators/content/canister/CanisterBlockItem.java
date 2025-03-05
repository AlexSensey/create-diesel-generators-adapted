package com.jesz.createdieselgenerators.content.canister;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.content.equipment.armor.CapacityEnchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class CanisterBlockItem extends BlockItem implements CapacityEnchantment.ICapacityEnchantable, FueledToolItem {
    public CanisterBlockItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltip, tooltipFlag);
        createTooltip(tooltip, stack);
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
    public void writeFluid(ItemStack stack, FluidStack fluid) {
        ListTag list = new ListTag();
        CompoundTag tankContent = new CompoundTag();
        tankContent.put("TankContent", fluid.writeToNBT(new CompoundTag()));
        list.add(tankContent);
        CompoundTag tag = new CompoundTag();
        tag.put("Tanks", list);
        stack.getOrCreateTag().put("BlockEntityTag", tag);
    }

    @Override
    public FluidStack readFluid(ItemStack stack) {
        return FluidStack.loadFluidStackFromNBT(stack.getOrCreateTag().getCompound("BlockEntityTag").getList("Tanks", Tag.TAG_COMPOUND).getCompound(0).getCompound("TankContent"));
    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_) {
        return super.useOn(p_40581_);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if(enchantment == AllEnchantments.CAPACITY.get())
            return true;
        return super.canApplyAtEnchantingTable(stack, enchantment);
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

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return getFluidHandler(stack);
    }
}
