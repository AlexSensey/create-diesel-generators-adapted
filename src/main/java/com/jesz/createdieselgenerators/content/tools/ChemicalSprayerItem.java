package com.jesz.createdieselgenerators.content.tools;

import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.content.tools.wire_cutters.WireCuttersItemRenderer;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Random;

public class ChemicalSprayerItem extends Item implements CustomArmPoseItem, FueledToolItem {
    boolean lighter;
    public ChemicalSprayerItem(Properties properties, boolean lighter) {
        super(properties);
        this.lighter = lighter;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        createTooltip(tooltipComponents, stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
        if (player.swinging)
            return null;
        return HumanoidModel.ArmPose.CROSSBOW_HOLD;
    }
    @Override
    public int getBarColor(ItemStack stack) {
        return 0xEFEFEF;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if(getCurrentFillLevel(stackInHand) > 0)
            player.startUsingItem(hand);
        return super.use(level, player, hand);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(AllEnchantments.CAPACITY))
            return true;
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {

        FluidStack fluidStack = readFluid(stack);
        if (!fluidStack.isEmpty()) {
            if (!level.isClientSide) {
                if (count % 2 == 0) {
                    boolean fire = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluidStack.getFluid()).normal().speed() != 0;
                    ChemicalSprayerProjectileEntity projectile = ChemicalSprayerProjectileEntity.spray(level, fluidStack, (fire && lighter) || fluidStack.getFluid().isSame(Fluids.LAVA), fluidStack.getFluid().isSame(Fluids.WATER));
                    projectile.setPos(player.position().add(0, 1.5f, 0));
                    projectile.shootFromRotation(player, player.getXRot() + new Random().nextFloat(-5, 5), player.getYRot() + new Random().nextFloat(-5, 5), 0.0f, 1.0f, 1.0f);
                    level.addFreshEntity(projectile);
                    fluidStack.setAmount(fluidStack.getAmount() - 1);
                }
                if (!(player instanceof Player p && p.isCreative()) && count % 25 == 0)
                    writeFluid(stack, fluidStack);
            } else {
                if (count % 2 == 0) {
                    AllSoundEvents.MIXING.playAt(level, player.blockPosition(), .75f, 1, true);
                }
            }
        }
        super.onUseTick(level, player, stack, count);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 1000;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCurrentFillLevel(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (getCurrentFillLevel(stack) / (float) getCapacity(stack)));
    }

    @OnlyIn(Dist.CLIENT)
    public void registerExtension(RegisterClientExtensionsEvent event) {
        event.registerItem(SimpleCustomRenderer.create(this, new WireCuttersItemRenderer()), this);
    }
}
